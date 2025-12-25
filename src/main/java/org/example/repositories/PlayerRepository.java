package org.example.repositories;

import org.example.db.DBConnection;
import org.example.entities.Player;
import org.example.entities.PlayerDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PlayerRepository {

    public void addPlayer(Player player) throws SQLException, ClassNotFoundException{
        Connection connection = DBConnection.getConnection();
        connection.setAutoCommit(false);

        String sqlInsert = """
            insert into player(id, name, score) values (?, ?, ?);
            """;

        PreparedStatement statement = connection.prepareStatement(sqlInsert);
        statement.setInt   (1, player.getId());
        statement.setString(2, player.getName());
        statement.setInt   (3, player.getOwned().size());
        statement.executeUpdate();

        statement.close();
        connection.commit();
        connection.close();
    }

    public int getNextId() throws SQLException, ClassNotFoundException{
        Connection connection = DBConnection.getConnection();
        connection.setAutoCommit(false);

        String sqlSelectId = "select id from nextval('player_seq') as id";
        PreparedStatement statement = connection.prepareStatement(sqlSelectId);
        ResultSet resultSet = statement.executeQuery();

        int id = -1;
        if(resultSet.next()){
            id = resultSet.getInt("id");
        }

        resultSet.close();
        statement.close();

        return id;
    }

    public List<PlayerDTO> getAll() throws SQLException, ClassNotFoundException {
        Connection connection = DBConnection.getConnection();
        List<PlayerDTO> players = new ArrayList<>();
        String sql = """
                        select id, name, score
                        from player
                        order by score desc
                        """;
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet resultSet = statement.executeQuery();

        while(resultSet.next()){
            players.add(new PlayerDTO(
                    resultSet.getInt("id"),
                    resultSet.getString("name"),
                    resultSet.getInt("score")
            ));
        }

        statement.close();
        resultSet.close();
        connection.close();

        return players;
    }
}