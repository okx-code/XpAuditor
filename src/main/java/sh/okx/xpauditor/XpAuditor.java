package sh.okx.xpauditor;

import com.zaxxer.hikari.HikariDataSource;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.utils.IOUtil;
import org.json.JSONObject;
import sh.okx.xpauditor.xp.Material;
import sh.okx.xpauditor.xp.Nation;
import sh.okx.xpauditor.xp.NationCount;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class XpAuditor {
  private HikariDataSource pool;

  public static void main(String[] args) throws LoginException, IOException {
    JSONObject json = new JSONObject(new String(IOUtil.readFully(XpAuditor.class.getResourceAsStream("/config.json"))));

    new XpAuditor(new JDABuilder(AccountType.BOT)
        .setToken(json.getString("token"))
        .setGame(Game.watching("XP Resources"))
        .build(), json.getString("database_password"));
  }


  public Nation getNation(Member member) {
    for (Role role : member.getRoles()) {
      for (Nation nation : getNations()) {
        if (nation.toString().equalsIgnoreCase(role.getName())) {
          return nation;
        }
      }
    }
    return null;
  }

  private XpAuditor(JDA jda, String password) {
    try {
      Class.forName("org.mariadb.jdbc.Driver");

      pool = new HikariDataSource();
      pool.setJdbcUrl("jdbc:mariadb://localhost/nca");
      pool.setUsername("nca");
      pool.setPassword("nca");
//      pool.setPassword(password);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    try (Connection connection = pool.getConnection()) {
      connection.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS nations (" +
          "`nation` VARCHAR(64) NOT NULL," +
          "PRIMARY KEY (`nation`))");
      connection.createStatement().executeUpdate(
          "CREATE TABLE IF NOT EXISTS xp (" +
              "`nation` VARCHAR(64) NOT NULL," +
              "`material` VARCHAR(64) NOT NULL," +
              "`amount` INT NOT NULL," +
              "PRIMARY KEY (`nation`, `material`)," +
              "FOREIGN KEY (`nation`)" +
              "  REFERENCES `nations` (`nation`)" +
              "  ON DELETE CASCADE)");
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

    jda.addEventListener(new CommandManager(this));
  }

  public List<Nation> getNations() {
    List<Nation> nations = new LinkedList<>();
    try (Connection connection = pool.getConnection()) {
      PreparedStatement statement = connection.prepareStatement("SELECT `nation` FROM `nations`");
      ResultSet resultSet = statement.executeQuery();

      while (resultSet.next()) {
        nations.add(new Nation(resultSet.getString(1)));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return nations;
  }

  public boolean addNation(String nation) {
    try (Connection connection = pool.getConnection()) {
      PreparedStatement statement = connection.prepareStatement("INSERT INTO `nations` VALUES (?)");
      statement.setString(1, nation);

      return statement.executeUpdate() > 0;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  public boolean deleteNation(String nation) {
    try (Connection connection = pool.getConnection()) {
      PreparedStatement statement = connection.prepareStatement("DELETE FROM `nations` WHERE `nation`=?");
      statement.setString(1, nation);

      return statement.executeUpdate() > 0;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  public boolean mergeNation(String first, String second) {
    try (Connection connection = pool.getConnection()) {
      try {
        connection.setAutoCommit(true);
        PreparedStatement replace = connection.prepareStatement("REPLACE INTO `xp` (SELECT ?, `material` AS `nmaterial`, (SELECT SUM(`amount`) FROM `xp` WHERE `material`=`nmaterial` AND (`nation`=? OR `nation`=?)) FROM `xp` WHERE `nation`=?)");
        replace.setString(1, second);
        // next two are interchangeable
        replace.setString(2, first);
        replace.setString(3, second);
        replace.setString(4, first);
        replace.executeUpdate();

        PreparedStatement delete = connection.prepareStatement("DELETE FROM `nations` WHERE `nation`=?");
        delete.setString(1, first);
        boolean returnValue = delete.executeUpdate() > 0;

        connection.commit();
        return returnValue;
      } finally {
        connection.setAutoCommit(false);
      }

    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  public void deposit(int amount, Material material, Nation nation) {
    try (Connection connection = pool.getConnection();
         PreparedStatement statement = connection.prepareStatement(
             "INSERT INTO xp VALUES (?, ?, ?) " +
                 "ON DUPLICATE KEY UPDATE amount=amount+?")) {
      statement.setString(1, nation.toString());
      statement.setString(2, material.name());
      statement.setInt(3, amount);
      statement.setInt(4, amount);

      statement.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public boolean withdraw(int amount, Material material, Nation nation) {
    if (getCount(nation, material) < amount) {
      return false;
    }

    deposit(-amount, material, nation);
    return true;
  }

  public Map<Nation, Integer> getContributions() {
    try (Connection connection = pool.getConnection()) {
      Map<Nation, Integer> contributions = new HashMap<>();

      PreparedStatement statement = connection.prepareStatement(
          "SELECT `nation`, SUM(`amount`) FROM xp GROUP BY nation");
      ResultSet results = statement.executeQuery();
      while (results.next()) {
        contributions.put(new Nation(results.getString(1)), results.getInt(2));
      }

      return contributions;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public int getCount(Material material) {
    try (Connection connection = pool.getConnection();
         PreparedStatement statement = connection.prepareStatement(
             "SELECT amount FROM xp WHERE material=?")) {
      statement.setString(1, material.name());

      return sum(statement.executeQuery());
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public int getCount(Nation nation, Material material) {
    try (Connection connection = pool.getConnection();
         PreparedStatement statement = connection.prepareStatement(
             "SELECT amount FROM xp WHERE nation=? AND material=?")) {
      statement.setString(1, nation.toString());
      statement.setString(2, material.name());

      return sum(statement.executeQuery());
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public Map<Nation, Integer> withdrawBatch(Material material) {
    int needed = material.getAmountNeeded() * 64;
    Map<Nation, Integer> counts = new HashMap<>();

    PriorityQueue<NationCount> queue = new PriorityQueue<>();
    for (Nation nation : getNations()) {
      queue.add(new NationCount(this, nation, material));
    }

    while (!queue.isEmpty() && needed > 0) {
      NationCount count = queue.poll();

      int amount = Math.max(0, Math.min(needed, count.getAmount()));
      needed -= count.getAmount();

      deposit(-amount, count.getMaterial(), count.getNation());
      counts.put(count.getNation(), counts.getOrDefault(count.getNation(), 0) + getPercentage(amount, material.getAmountNeeded() * 64));
    }

    return counts;
  }

  private int getPercentage(double amount, double max) {
    return (int) ((amount / max) * 10_000);
  }

  public Map<Material, Integer> getAmounts() {
    Map<Material, Integer> amounts = new LinkedHashMap<>();
    for (Material material : Material.values()) {
      amounts.put(material, getCount(material));
    }
    return amounts;
  }

  private int sum(ResultSet rs) {
    int sum = 0;
    try {
      while (rs.next()) {
        sum += rs.getInt("amount");
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return sum;
  }
}
