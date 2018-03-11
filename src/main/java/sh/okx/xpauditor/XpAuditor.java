package sh.okx.xpauditor;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.utils.IOUtil;
import org.json.JSONObject;
import sh.okx.sql.ConnectionBuilder;
import sh.okx.sql.api.Connection;
import sh.okx.sql.api.PooledConnection;
import sh.okx.sql.api.query.QueryResults;
import sh.okx.xpauditor.xp.Material;
import sh.okx.xpauditor.xp.Nation;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class XpAuditor {
  public static void main(String[] args) throws LoginException, RateLimitedException, IOException {
    JSONObject json = new JSONObject(new String(IOUtil.readFully(XpAuditor.class.getResourceAsStream("/config.json"))));

    new XpAuditor(new JDABuilder(AccountType.BOT)
        .setToken(json.getString("token"))
        .setGame(Game.watching("XP Resources"))
        .buildAsync(), json.getString("database_password"));
  }

  private JDA jda;
  private PooledConnection pool;

  private XpAuditor(JDA jda, String password) {
    this.jda = jda;
    this.pool = new ConnectionBuilder()
        .setCredentials("root", password)
        .setDatabase("nca")
        .buildPool();

    getConnection().thenAccept(connection -> {
      connection.table("contrib_user").create()
          .setIfNotExists(true)
          .column("user INT(20) PRIMARY KEY")
          .column("amount INT(8) DEFAULT 0")
          .execute();
      connection.table("contrib").create()
          .setIfNotExists(true)
          .column("nation VARCHAR(16) PRIMARY KEY")
          .column("amount INT(8) DEFAULT 0")
          .execute();
      connection.table("xp").create()
          .setIfNotExists(true)
          .column("material VARCHAR(36) PRIMARY KEY")
          .column("amount INT(8)")
          .execute();
    });


    jda.addEventListener(new CommandManager(this));
  }

  public CompletableFuture<Map<User, Integer>> getUserContributions() {
    return getConnection().thenApply(connection -> {
      QueryResults results = connection.table("contrib_user").select().execute();
      Map<User, Integer> map = new HashMap<>();

      ResultSet resultSet = results.getResultSet();
      try {
        while(resultSet.next()) {
          User user = jda.getUserById(resultSet.getString("user"));
          if(user == null) {
            continue;
          }

          map.put(user, resultSet.getInt("amount"));
        }
      } catch(SQLException ex) {
        ex.printStackTrace();
        return null;
      }

      return map;
    });
  }

  public CompletableFuture<Integer> getTotalUserContributions() {
    return getConnection().thenApply(connection -> {
      QueryResults results = connection.table("contrib_user").select().execute();
      int sum = 0;

      ResultSet resultSet = results.getResultSet();
      try {
        while(resultSet.next()) {
          sum += resultSet.getInt("amount");
        }
      } catch(SQLException ex) {
        ex.printStackTrace();
        return null;
      }

      return sum;
    });
  }

  public Nation getNation(Member member) {
    for (Role role : member.getRoles()) {
      for (Nation nation : Nation.values()) {
        if (nation.toString().equalsIgnoreCase(role.getName())) {
          return nation;
        }
      }
    }
    return null;
  }

  public CompletableFuture<Connection> getConnection() {
    return CompletableFuture.supplyAsync(() -> pool.getConnection());
  }

  public void deposit(int amount, Material material, Member member) {
    Nation nation = getNation(member);
    String id = member.getUser().getId();

    getConnection().thenAccept(connection -> {
      connection.executeUpdate(
          "INSERT INTO xp SET material=?, amount=? " +
              "ON DUPLICATE KEY UPDATE amount=amount+?",
          material.name(), amount + "", amount + "");
      connection.executeUpdate(
          "INSERT INTO contrib SET nation=?, amount=? " +
              "ON DUPLICATE KEY UPDATE amount=amount+?",
          nation.name(), amount + "", amount + "");
      connection.executeUpdate(
          "INSERT INTO contrib_user SET user=?, amount=? " +
              "ON DUPLICATE KEY UPDATE amount=amount+?",
          id, -amount + "", amount + "");
    });
  }

  public CompletableFuture<Boolean> withdraw(int amount, Material material, Member member) {
    Nation nation = getNation(member);
    String id = member.getUser().getId();

    return getConnection().thenApply(connection -> {
      QueryResults results = connection.table("xp")
          .select("amount")
          .where().prepareEquals("material", material.name()).then()
          .execute();
      if (!results.next()) {
        // material is not available to withdraw
        return false;
      }

      try {
        int existingAmount = results.getResultSet().getInt("amount");
        if (existingAmount < amount) {
          // not enough of the material
          return false;
        }
      } catch (SQLException e) {
        e.printStackTrace();
      }

      connection.executeUpdate(
          "INSERT INTO xp SET material=?, amount=? " +
              "ON DUPLICATE KEY UPDATE amount=amount-?",
          material.name(), -amount + "", amount + "");
      connection.executeUpdate(
          "INSERT INTO contrib SET nation=?, amount=? " +
              "ON DUPLICATE KEY UPDATE amount=amount-?",
          nation.name(), -amount + "", amount + "");
      connection.executeUpdate(
          "INSERT INTO contrib_user SET user=?, amount=? " +
              "ON DUPLICATE KEY UPDATE amount=amount-?",
          id, -amount + "", amount + "");

      return true;
    });
  }

  public CompletableFuture<Integer> getContribution(Nation nation) {
    return getConnection().thenApply(connection -> {
      QueryResults results = connection.table("contrib")
          .select("amount")
          .where().prepareEquals("nation", nation.name()).then()
          .execute();
      if (!results.next()) {
        return 0;
      }
      try {
        return results.getResultSet().getInt("amount");
      } catch (SQLException e) {
        return 0;
      }
    });
  }

  public CompletableFuture<Integer> getContribution(Member member) {
    String id = member.getUser().getId();

    return getConnection().thenApply(connection -> {
      QueryResults results = connection.table("contrib_user")
          .select("amount")
          .where().prepareEquals("user", id).then()
          .execute();
      if (!results.next()) {
        return 0;
      }
      try {
        return results.getResultSet().getInt("amount");
      } catch (SQLException e) {
        return 0;
      }
    });
  }

  public CompletableFuture<Integer> getCount(Material material) {
    return getConnection().thenApply(connection -> {
      QueryResults results = connection.table("xp")
          .select("amount")
          .where().prepareEquals("material", material.name()).then()
          .execute();
      if (!results.next()) {
        return 0;
      }
      try {
        return results.getResultSet().getInt("amount");
      } catch (SQLException e) {
        return 0;
      }
    });
  }
}
