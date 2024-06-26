package com.application.labgui.Repository;

import com.application.labgui.Domain.Friendship;
import com.application.labgui.Domain.Tuplu;
import com.application.labgui.Validators.FactoryValidator;
import com.application.labgui.Validators.Validator;
import com.application.labgui.Validators.ValidatorStrategies;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;

public class FriendshipDBRepository implements Repository<Tuplu<Long, Long>, Friendship>{
    private Validator prietenieValidator;
    private DBConnection dbConnection;

    public FriendshipDBRepository(DBConnection dbConnection, ValidatorStrategies validatorStrategies) {
        prietenieValidator = FactoryValidator.getFactoryInstance().createValidator(validatorStrategies);
        this.dbConnection = dbConnection;
    }

    @Override
    public Optional<Friendship> findOne(Tuplu<Long, Long> longLongTuplu) {
        try(Connection connection = DriverManager.getConnection(dbConnection.DB_URL, dbConnection.DB_USER, dbConnection.DB_PASSWD);
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM Prietenii " +
                    "WHERE (iduser1 = ? AND iduser2 = ?) OR (iduser2 = ? AND iduser1 = ?)");

        ) {
            statement.setLong(1, longLongTuplu.getLeft());
            statement.setLong(2, longLongTuplu.getRight());
            statement.setLong(3, longLongTuplu.getLeft());
            statement.setLong(4, longLongTuplu.getRight());
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()) {
                LocalDateTime friendsFrom = resultSet.getTimestamp("friendsFrom").toLocalDateTime();
                Friendship friendship = new Friendship(longLongTuplu.getLeft(), longLongTuplu.getRight(), friendsFrom);
                return Optional.of(friendship);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public Iterable<Friendship> findAll() {
        HashMap<Tuplu<Long, Long>, Friendship> entities = new HashMap<>();
        try (Connection connection = DriverManager.getConnection(dbConnection.DB_URL, dbConnection.DB_USER, dbConnection.DB_PASSWD)) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM prietenii");
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Long idUser1 = resultSet.getLong("iduser1");
                Long idUser2 = resultSet.getLong("iduser2");
                LocalDateTime friendsFrom = resultSet.getTimestamp("friendsFrom").toLocalDateTime();

                Friendship friendship = new Friendship(idUser1, idUser2, friendsFrom);
                entities.put(friendship.getId(), friendship);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return entities.values();
    }

    public Iterable<Friendship> findPrieteniiUser(Long idUser){
        HashMap<Tuplu<Long, Long>, Friendship> entities = new HashMap<>();
        try (Connection connection = DriverManager.getConnection(dbConnection.DB_URL, dbConnection.DB_USER, dbConnection.DB_PASSWD)) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM prietenii WHERE iduser1 = ? OR iduser2 = ?");
            statement.setLong(1, idUser);
            statement.setLong(2, idUser);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Long idUser1 = resultSet.getLong("iduser1");
                Long idUser2 = resultSet.getLong("iduser2");
                LocalDateTime friendsFrom = resultSet.getTimestamp("friendsFrom").toLocalDateTime();
                Friendship friendship = new Friendship(idUser1, idUser2, friendsFrom);
                entities.put(friendship.getId(), friendship);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return entities.values();
    }

    @Override
    public Optional<Friendship> save(Friendship entity) {
        prietenieValidator.validate(entity);
        String sqlStatement = "INSERT INTO prietenii(iduser1, iduser2, friendsfrom) VALUES (?, ?, ?);";
        var optional = findOne(entity.getId());
        if(optional.isPresent()){
            return Optional.of(entity);
        }
        try(Connection connection = DriverManager.getConnection(dbConnection.DB_URL, dbConnection.DB_USER, dbConnection.DB_PASSWD);
            PreparedStatement preparedStatement = connection.prepareStatement(sqlStatement)){
            preparedStatement.setLong(1, entity.getId().getLeft());
            preparedStatement.setLong(2, entity.getId().getRight());
            preparedStatement.setTimestamp(3, Timestamp.valueOf(entity.getDateCreated()));
            var responseSQL =preparedStatement.executeUpdate();
            return responseSQL==0 ? Optional.of(entity) : Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Friendship> delete(Tuplu<Long, Long> longLongTuplu) {
        String sqlStatement = "DELETE FROM prietenii P WHERE (P.iduser1 = ? AND P.iduser2 = ?) OR (P.iduser2 = ? AND P.iduser1 = ?)";
        try(Connection connection = DriverManager.getConnection(dbConnection.DB_URL, dbConnection.DB_USER, dbConnection.DB_PASSWD);
            PreparedStatement preparedStatement = connection.prepareStatement(sqlStatement)){
            preparedStatement.setLong(1, longLongTuplu.getLeft());
            preparedStatement.setLong(2, longLongTuplu.getRight());
            preparedStatement.setLong(3, longLongTuplu.getLeft());
            preparedStatement.setLong(4, longLongTuplu.getRight());
            var entitatea = findOne(longLongTuplu);
            var raspuns = 0;
            if(entitatea.isPresent()){
                raspuns = preparedStatement.executeUpdate();
            }
            return raspuns==0 ? Optional.empty() : entitatea;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public Optional<Friendship> update(Friendship entity) {
        String sqlStatement = "UPDATE prietenii P SET friendsfrom = ? WHERE (P.iduser1 = ? AND P.iduser2 = ?) OR (P.iduser2 = ? AND P.iduser1 = ?)";
        prietenieValidator.validate(entity);
        try(Connection connection = DriverManager.getConnection(dbConnection.DB_URL, dbConnection.DB_USER, dbConnection.DB_PASSWD);
            PreparedStatement preparedStatement = connection.prepareStatement(sqlStatement);
        ){
            preparedStatement.setTimestamp(1, Timestamp.valueOf(entity.getDateCreated()));
            preparedStatement.setLong(2 ,entity.getId().getLeft());
            preparedStatement.setLong(3 ,entity.getId().getRight());
            preparedStatement.setLong(4 ,entity.getId().getLeft());
            preparedStatement.setLong(5 ,entity.getId().getRight());
            var response = preparedStatement.executeUpdate();
            return response==0 ? Optional.of(entity) : Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int size() {
        try (Connection connection = DriverManager.getConnection(dbConnection.DB_URL, dbConnection.DB_USER, dbConnection.DB_PASSWD)) {
            PreparedStatement statement = connection.prepareStatement("SELECT count(*) FROM prietenii");
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            return resultSet.getInt(1);
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
