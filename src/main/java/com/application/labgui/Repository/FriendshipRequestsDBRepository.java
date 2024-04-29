package com.application.labgui.Repository;

import com.application.labgui.Domain.FriendshipRequest;
import com.application.labgui.Domain.Tuplu;
import com.application.labgui.Validators.FactoryValidator;
import com.application.labgui.Validators.Validator;
import com.application.labgui.Validators.ValidatorStrategies;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;

public class FriendshipRequestsDBRepository implements Repository<Tuplu<Long, Long>, FriendshipRequest>{
    private Validator cererePrietenieValidator;
    private DBConnection dbConnection;

    public FriendshipRequestsDBRepository(DBConnection dbConnection, ValidatorStrategies validatorStrategies) {
        cererePrietenieValidator = FactoryValidator.getFactoryInstance().createValidator(validatorStrategies);
        this.dbConnection = dbConnection;
    }

    @Override
    public Optional<FriendshipRequest> findOne(Tuplu<Long, Long> longLongTuplu) {
        try(Connection connection = DriverManager.getConnection(dbConnection.DB_URL, dbConnection.DB_USER, dbConnection.DB_PASSWD);
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM CereriPrietenie " +
                    "WHERE (iduser1 = ? AND iduser2 = ?) OR (iduser1 = ? AND iduser2 = ?)");

        ) {
            statement.setLong(1, longLongTuplu.getLeft());
            statement.setLong(2, longLongTuplu.getRight());
            statement.setLong(3, longLongTuplu.getRight());
            statement.setLong(4, longLongTuplu.getLeft());
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()) {
                Long idSender = resultSet.getLong("iduser1");
                Long idRecv = resultSet.getLong("iduser2");
                Integer status = resultSet.getInt("stare_cerere");
                LocalDateTime friendsFrom = null;
                var data = resultSet.getTimestamp("datecreated");
                if(data!=null){
                    friendsFrom = data.toLocalDateTime();
                }
                FriendshipRequest prietenie = new FriendshipRequest(idSender, idRecv, friendsFrom, status);
                return Optional.of(prietenie);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }


    @Override
    public Iterable<FriendshipRequest> findAll() {
        HashMap<Tuplu<Long, Long>, FriendshipRequest> entities = new HashMap<>();
        try (Connection connection = DriverManager.getConnection(dbConnection.DB_URL, dbConnection.DB_USER, dbConnection.DB_PASSWD)) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM cereriprietenie");
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Long idSender = resultSet.getLong("iduser1");
                Long idRecv = resultSet.getLong("iduser2");
                Integer status = resultSet.getInt("stare_cerere");
                LocalDateTime friendsFrom = null;
                var data = resultSet.getTimestamp("datecreated");
                if(data!=null){
                    friendsFrom = data.toLocalDateTime();
                }
                FriendshipRequest friendshipRequest = new FriendshipRequest(idSender, idRecv, friendsFrom, status);
                entities.put(friendshipRequest.getId(), friendshipRequest);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return entities.values();
    }

    public Iterable<FriendshipRequest> findCereriUser(Long idUser){
        HashMap<Tuplu<Long, Long>, FriendshipRequest> entities = new HashMap<>();
        try (Connection connection = DriverManager.getConnection(dbConnection.DB_URL, dbConnection.DB_USER, dbConnection.DB_PASSWD)) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM cereriprietenie WHERE iduser1 = ? OR iduser2 = ?");
            statement.setLong(1, idUser);
            statement.setLong(2, idUser);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Long idSender = resultSet.getLong("iduser1");
                Long idRecv = resultSet.getLong("iduser2");
                Integer status = resultSet.getInt("stare_cerere");
                LocalDateTime friendsFrom = null;
                var data = resultSet.getTimestamp("datecreated");
                if(data!=null){
                    friendsFrom = data.toLocalDateTime();
                }
                FriendshipRequest friendshipRequest = new FriendshipRequest(idSender, idRecv, friendsFrom, status);
                entities.put(friendshipRequest.getId(), friendshipRequest);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return entities.values();
    }

    @Override
    public Optional<FriendshipRequest> save(FriendshipRequest entity) {
        cererePrietenieValidator.validate(entity);
        if(findOne(entity.getId()).isPresent()){
            return Optional.of(entity);
        }
        String sqlStatement = "INSERT INTO cereriprietenie(iduser1, iduser2, datecreated, stare_cerere) VALUES (?, ?, ?, ?);";
        var optional = findOne(entity.getId());
        if(optional.isPresent()){
            return Optional.of(entity);
        }
        try(Connection connection = DriverManager.getConnection(dbConnection.DB_URL, dbConnection.DB_USER, dbConnection.DB_PASSWD);
            PreparedStatement preparedStatement = connection.prepareStatement(sqlStatement)){
            preparedStatement.setLong(1, entity.getId().getLeft());
            preparedStatement.setLong(2, entity.getId().getRight());
            if(entity.getDateCreated()==null){
                preparedStatement.setTimestamp(3, null);
            }
            else{
                preparedStatement.setTimestamp(3, Timestamp.valueOf(entity.getDateCreated()));
            }
            preparedStatement.setInt(4, entity.getStatus());
            var responseSQL =preparedStatement.executeUpdate();
            return responseSQL==0 ? Optional.of(entity) : Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<FriendshipRequest> delete(Tuplu<Long, Long> longLongTuplu) {
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
    public Optional<FriendshipRequest> update(FriendshipRequest entity) {
        String sqlStatement = "UPDATE cereriprietenie P SET datecreated = ?, stare_cerere = ? WHERE (P.iduser1 = ? AND P.iduser2 = ?) OR (P.iduser2 = ? AND P.iduser1 = ?)";
        cererePrietenieValidator.validate(entity);
        try(Connection connection = DriverManager.getConnection(dbConnection.DB_URL, dbConnection.DB_USER, dbConnection.DB_PASSWD);
            PreparedStatement preparedStatement = connection.prepareStatement(sqlStatement);
        ){
            if(entity.getDateCreated()==null){
                preparedStatement.setTimestamp(1, null);
            }
            else{
                preparedStatement.setTimestamp(1, Timestamp.valueOf(entity.getDateCreated()));
            }
            preparedStatement.setInt(2, entity.getStatus());
            preparedStatement.setLong(3 ,entity.getId().getLeft());
            preparedStatement.setLong(4 ,entity.getId().getRight());
            preparedStatement.setLong(5 ,entity.getId().getLeft());
            preparedStatement.setLong(6 ,entity.getId().getRight());
            var response = preparedStatement.executeUpdate();
            return response==0 ? Optional.of(entity) : Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int size() {
        try (Connection connection = DriverManager.getConnection(dbConnection.DB_URL, dbConnection.DB_USER, dbConnection.DB_PASSWD)) {
            PreparedStatement statement = connection.prepareStatement("SELECT count(*) FROM cereriprietenie");
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            return resultSet.getInt(1);
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
