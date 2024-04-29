package com.application.labgui.Repository;

import com.application.labgui.Domain.User;
import com.application.labgui.Repository.Paging.IPagingRepository;
import com.application.labgui.Repository.Paging.Page;
import com.application.labgui.Repository.Paging.Pageable;
import com.application.labgui.Validators.FactoryValidator;
import com.application.labgui.Validators.Validator;
import com.application.labgui.Validators.ValidatorStrategies;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;


public class UserDBRepository implements IPagingRepository<Long, User> {
    private Validator utilizatorValidator;
    private DBConnection dbConnection;

    public UserDBRepository(DBConnection dbConnection, ValidatorStrategies validatorStrategies) {
        utilizatorValidator = FactoryValidator.getFactoryInstance().createValidator(validatorStrategies);
        this.dbConnection = dbConnection;
    }


    @Override
    public Optional<User> findOne(Long longID) {
        try(Connection connection = DriverManager.getConnection(dbConnection.DB_URL, dbConnection.DB_USER, dbConnection.DB_PASSWD);
            PreparedStatement statement = connection.prepareStatement("select * from Utilizatori " +
                    "where idUser = ?");

        ) {
            statement.setLong(1, longID);
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()) {
                String firstName = resultSet.getString("firstname");
                String lastName = resultSet.getString("lastname");
                User u = new User(firstName,lastName);
                u.setId(longID);
                u = loadFriends(u).get();
                return Optional.ofNullable(u);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }


    @Override
    public Iterable<User> findAll() {
        HashMap<Long, User> entities = new HashMap<>();
        try (Connection connection = DriverManager.getConnection(dbConnection.DB_URL, dbConnection.DB_USER, dbConnection.DB_PASSWD)) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM Utilizatori");
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Long id = resultSet.getLong("iduser");
                String firstName = resultSet.getString("firstname");
                String lastName = resultSet.getString("lastname");
                User user = new User(firstName, lastName);
                user.setId(id);
                user = loadFriends(user).get();
                entities.put(id, user);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return entities.values();
    }

    @Override
    public Optional<User> save(User entity) {
        utilizatorValidator.validate(entity);
        try(Connection connection = DriverManager.getConnection(dbConnection.DB_URL, dbConnection.DB_USER, dbConnection.DB_PASSWD);
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO Utilizatori(firstname, lastname,username,password) VALUES(?, ?,?,?);")){
            preparedStatement.setString(1, entity.getFirstName());
            preparedStatement.setString(2, entity.getLastName());
            preparedStatement.setString(3,entity.getUsername());
            preparedStatement.setString(4,entity.getHashedPassword());
            var responseSQL =preparedStatement.executeUpdate();
            return responseSQL==0 ? Optional.of(entity) : Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<User> delete(Long aLong) {
        try(Connection connection = DriverManager.getConnection(dbConnection.DB_URL, dbConnection.DB_USER, dbConnection.DB_PASSWD);
            PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM Utilizatori U WHERE U.iduser = ?")){
            preparedStatement.setLong(1, aLong);
            var entitatea = findOne(aLong);
            var raspuns = 0;
            if(entitatea.isPresent()){
                raspuns = preparedStatement.executeUpdate();
            }
            return raspuns==0 ? Optional.empty() : entitatea;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Optional<User> loadFriends(User user){
        String sqlStatement = "SELECT U.* FROM Utilizatori U, Prietenii P\n" +
                "WHERE (P.iduser1 = ? AND P.iduser2 = u.IDUser) OR (P.iduser2 = ? AND P.iduser1 = u.IDUser);";
        try(Connection connection = DriverManager.getConnection(dbConnection.DB_URL, dbConnection.DB_USER, dbConnection.DB_PASSWD);
            PreparedStatement preparedStatement = connection.prepareStatement(sqlStatement)
        ) {
            preparedStatement.setLong(1, user.getId());
            preparedStatement.setLong(2, user.getId());
            var resultSet =preparedStatement.executeQuery();
            while (resultSet.next()) {
                Long id = resultSet.getLong("iduser");
                String firstName = resultSet.getString("firstname");
                String lastName = resultSet.getString("lastname");
                User userFriend = new User(firstName, lastName);
                userFriend.setId(id);
                user.addFriend(userFriend);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.of(user);
    }

    @Override
    public Optional<User> update(User entity) {
        utilizatorValidator.validate(entity);
        try(Connection connection = DriverManager.getConnection(dbConnection.DB_URL, dbConnection.DB_USER, dbConnection.DB_PASSWD);
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE Utilizatori SET firstname = ?, lastname=? WHERE idUser = ?")
        ){
            preparedStatement.setString(1, entity.getFirstName());
            preparedStatement.setString(2, entity.getLastName());
            preparedStatement.setLong(3 ,entity.getId());
            var response = preparedStatement.executeUpdate();
            return response==0 ? Optional.of(entity) : Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int size() {
        try(Connection connection = DriverManager.getConnection(dbConnection.DB_URL, dbConnection.DB_USER,
                dbConnection.DB_PASSWD);
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(*) FROM utilizatori")
        ) {
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            return resultSet.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Page<User> findAll(Pageable pageable)
    {
        int numberOfElements = size();
        int limit = pageable.getPageSize();
        int offset = pageable.getPageSize()*pageable.getPageNumber();
        System.out.println(offset + " ?>= "+numberOfElements);
        if(offset >= numberOfElements)
            return new Page<>(new ArrayList<>(), numberOfElements);


        List<User> users = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(dbConnection.DB_URL,
                dbConnection.DB_USER, dbConnection.DB_PASSWD);
            PreparedStatement statement = connection.prepareStatement
                    ("select * from utilizatori offset ? limit ?");
        )
        {
            statement.setInt(1, offset);
            statement.setInt(2,limit);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()){
                Long id = resultSet.getLong("iduser");
                String firstname = resultSet.getString("firstname");
                String lastnaem = resultSet.getString("lastname");
                String usernaem=resultSet.getString("username");

                User currentUser = new User(firstname,lastnaem,usernaem);
                currentUser.setId(id);
                users.add(currentUser);
            }
        }
        catch (SQLException e){
            System.out.println(e.getMessage());
        }
        return new Page<>(users, numberOfElements);
    }
    public Optional<User> findOnesAccount(String username, String hashedPassword)
    {
        try(Connection connection = DriverManager.getConnection(dbConnection.DB_URL, dbConnection.DB_USER, dbConnection.DB_PASSWD);
            PreparedStatement statement = connection.prepareStatement("select * from Utilizatori " +
                    "where username = ? and password=?");

        ) {
            statement.setString(1,username);
            statement.setString(2,hashedPassword);
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()) {
                Long longID=resultSet.getLong(1);
                String firstName = resultSet.getString("firstname");
                String lastName = resultSet.getString("lastname");
                User u = new User(firstName,lastName,username);
                u.setId(longID);
                u = loadFriends(u).get();
                return Optional.ofNullable(u);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }
}
