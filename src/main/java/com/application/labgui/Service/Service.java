package com.application.labgui.Service;

import com.application.labgui.AppExceptions.ServiceException;
import com.application.labgui.AppExceptions.ValidationException;
import com.application.labgui.Domain.*;
import com.application.labgui.Repository.FriendshipRequestsDBRepository;
import com.application.labgui.Repository.MessageDBRepository;
import com.application.labgui.Repository.Paging.Page;
import com.application.labgui.Repository.Paging.Pageable;
import com.application.labgui.Repository.Repository;
import com.application.labgui.Repository.UserDBRepository;
import com.application.labgui.Utils.DFS;
import com.application.labgui.Utils.Events.ChangeEventType;
import com.application.labgui.Utils.Events.ServiceChangeEvent;
import com.application.labgui.Utils.Observer.Observable;
import com.application.labgui.Validators.FactoryValidator;
import com.application.labgui.Validators.Validator;
import com.application.labgui.Validators.ValidatorStrategies;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
public class Service implements Observable<ServiceChangeEvent> {
    private UserDBRepository repositoryUtilizatori;
    private Repository<Tuplu<Long, Long>, Friendship> repositoryPrietenii;

    private FriendshipRequestsDBRepository repositoryCereriPrietenii;

    private MessageDBRepository repositoryMesaje;
    private Validator validatorUtilizator;
    private Validator validatorPrietenie;
    private Validator validatorCererePrietenie;


    /**
     * constructor service
     *
     * @param repositoryUtilizatori
     * @param repositoryCereriPrietenii
     * @param repositoryPrietenii
     * @param repositoryMesaje
     * @param strategies                strategie de validare pentru utilizator
     * @param strategies1               strategie de validare pentru prietenie
     */
    public Service(UserDBRepository repositoryUtilizatori, FriendshipRequestsDBRepository repositoryCereriPrietenii, Repository repositoryPrietenii, MessageDBRepository repositoryMesaje, ValidatorStrategies strategies, ValidatorStrategies strategies1) {
        this.repositoryUtilizatori = repositoryUtilizatori;
        this.repositoryCereriPrietenii = repositoryCereriPrietenii;
        this.repositoryPrietenii = repositoryPrietenii;
        this.repositoryMesaje = repositoryMesaje;
        var factory = FactoryValidator.getFactoryInstance();
        this.validatorUtilizator = factory.createValidator(strategies);
        this.validatorPrietenie = factory.createValidator(strategies1);
        this.validatorCererePrietenie = factory.createValidator(ValidatorStrategies.CEREREPRIETENIE);
    }

    /**
     * @param idSender       - cel cae trimite mesajul
     * @param idDestinations - lista cu cei la care se trimite mesajul(care e de tip singleton si contine un singur user)
     * @param textDeTrimis   - mesjul
     * @param dataTrimiterii data
     * @param
     */
    public void sentNewMessage(Long idSender, List<Long> idDestinations, String textDeTrimis, LocalDateTime dataTrimiterii) {
        Message messageNou = new Message(idSender, idDestinations, textDeTrimis, dataTrimiterii);
        var response = repositoryMesaje.save(messageNou);
        if (response.isPresent()) {
            throw new ServiceException("Mesajul nu a putut fi trimis!");
        }
        //instintam observerii de modificare
        this.notifyAllObservers(new ServiceChangeEvent(ChangeEventType.MESSAGES, idSender, idDestinations.get(0)));
    }

    public void sentNewMessage(Long idSender, Long idRecv, Long idMesajReplyTo, String textDeTrimis, LocalDateTime dataTrimiterii) {
        var replyTo = repositoryMesaje.findOne(idMesajReplyTo).get(); // cautam mesajul la care se da rply in db
        Message messageNou = new Message(idSender, idRecv, replyTo, textDeTrimis, dataTrimiterii);
        var response = repositoryMesaje.save(messageNou);
        if (response.isPresent()) {
            throw new ServiceException("Mesajul nu a putut fi trimis!");
        }
        var replyToID = replyTo.getFromUser();
        this.notifyAllObservers(new ServiceChangeEvent(ChangeEventType.MESSAGES, idSender, idRecv));
    }

    public Optional<Message> findOneMessage(Long aLong) {
        return repositoryMesaje.findOne(aLong);
    }

    public Iterable<Message> getAllMessagesBetween(Long user1, Long user2) {
        var lista = this.repositoryMesaje.findMesajeBetween(user1, user2);
        return StreamSupport.stream(lista.spliterator(), false).sorted((x, y) -> {
            if (x.getData().equals(y.getData())) {
                return 0;
            }
            var i = x.getData().isBefore(y.getData());
            return !i ? 1 : -1;
        }).collect(Collectors.toList());
    }

    public Iterable<Message> getAllMessages() {
        return this.repositoryMesaje.findAll();
    }

    /**
     * functie de adaugare a unui nou user
     *
     * @param numeUtilizator    numele noului utilizator, string nevid
     * @param prenumeUtilizator prenumele noului utilizator, string nevid
     * @throws ServiceException    daca exista deja utilizatorul in repo
     * @throws ValidationException daca stringurile sunt vide
     */
    public void addNewUser(String numeUtilizator, String prenumeUtilizator, String username, String password) throws Exception {


        if(password.isEmpty()){
            throw  new ServiceException("CAMP PAROLA GOL");}

        AES aes=AES.getInstance();
        byte[]  hashedPasswordArrayByte=aes.encryptMessage(password.getBytes());
        String hashedPassword = Base64.getEncoder().encodeToString(hashedPasswordArrayByte);
        var utilizatorNou = new User(prenumeUtilizator, numeUtilizator,username,hashedPassword);
        validatorUtilizator.validate(utilizatorNou);
        System.out.println(password+ " " + hashedPassword);
        var response = repositoryUtilizatori.save(utilizatorNou);
        if (response.isPresent()) {
            throw new ServiceException(" Utilizatorului existent!");
        }
        this.notifyAllObservers(new ServiceChangeEvent());
    }

    /**
     * getter pentru toti utilzatorii
     * @return un iterabil
     */
    public Iterable<User> getAllUtilizatori() {
        Stream<User> myStream = StreamSupport.stream(repositoryUtilizatori.findAll().spliterator(), false);
        return myStream
                .sorted((x, y) -> x.compareTo(y))
                .collect(Collectors.toList());
    }


    public Page<User> getUsersOnPage(Pageable pageable) {
        return repositoryUtilizatori.findAll(pageable);
    }

    /**
     * functie de stergere a unui utilizator si a prieteniilor acestuia
     * @param idUtilizator idul utilizatorului care urmeaza a fi sters
     * @throws ServiceException daca utilizatorul nu exista
     */
    public void deleteUtilizator(Long idUtilizator) {
        var response = this.repositoryUtilizatori.delete(idUtilizator);
        if (response.isEmpty()) {
            throw new ServiceException("Utilizator inexistent!");
        }
        var u1 = response.get();
        if (u1.getFriends().isEmpty()) {
            this.notifyAllObservers(new ServiceChangeEvent());
            return;
        }
        u1.getFriends().forEach(x -> {
            repositoryPrietenii.delete(new Tuplu<>(u1.getId(), x.getId()));
            x.deleteFriend(u1);
            repositoryUtilizatori.update(x);
        });
        this.notifyAllObservers(new ServiceChangeEvent());
    }

    /**
     * functie de adaugare a unei relatii de prietenie intre 2 utilziatori
     * @param idU1 id-ul primului user
     * @param idU2 id-ul celui de-al dooilea user
     * @throws ValidationException daca id-urile nu sunt valide
     * @throws ServiceException    daca nu exista userii sau daca relatia de prietenie exista deja
     */
    public void addPrietenie(Long idU1, Long idU2) {
        Predicate<Long> existaUser = idUser -> repositoryUtilizatori.findOne(idUser).isPresent();
        Consumer<User> updateUser = repositoryUtilizatori::update;
        Friendship friendship = new Friendship(idU1, idU2);
        this.validatorPrietenie.validate(friendship);
        if (!(existaUser.test(idU1) && existaUser.test(idU2))) {
            throw new ServiceException("Nu exista userii!");
        }
        User u1 = repositoryUtilizatori.findOne(idU1).get();
        User u2 = repositoryUtilizatori.findOne(idU2).get();
        var response = repositoryPrietenii.save(friendship);
        if (response.isPresent()) {
            throw new ServiceException("Relatia de prietenie exista deja!");
        }
        u1.addFriend(u2);
        u2.addFriend(u1);
        updateUser.accept(u1);
        updateUser.accept(u2);
        this.notifyAllObservers(new ServiceChangeEvent(ChangeEventType.FRIENDS, idU1, idU2));
    }

    public void trimiteCererePrietenie(Long idUserSender, Long idUserRecv) {
        Predicate<Long> existaUser = idUser -> repositoryUtilizatori.findOne(idUser).isPresent();
        if (!(existaUser.test(idUserSender) && existaUser.test(idUserRecv))) {
            throw new ServiceException("Nu exista userii!");
        }
        FriendshipRequest friendshipRequest = new FriendshipRequest(idUserSender, idUserRecv);
        this.validatorCererePrietenie.validate(friendshipRequest);
        var response = repositoryCereriPrietenii.save(friendshipRequest);
        if (response.isPresent()) {
            throw new ServiceException("Cererea de prietenie exista deja!");
        }
        this.notifyAllObservers(new ServiceChangeEvent(ChangeEventType.FRIENDS, idUserRecv, idUserSender));
    }

    public void acceptCererePrietenie(Long idUserSender, Long idUserRecv) {
        var cerereMomentanaOpt = this.repositoryCereriPrietenii.findOne(new Tuplu<>(idUserSender, idUserRecv));
        if (cerereMomentanaOpt.isEmpty()) {
            throw new ServiceException("Nu exista1");
        }
        var cerereMomentan = cerereMomentanaOpt.get();
        cerereMomentan.setAccepted();
        var entitatea = this.repositoryCereriPrietenii.update(cerereMomentan);
        if (entitatea.isPresent()) {
            throw new ServiceException("Ceva nu a mers bine");
        }
        Consumer<User> updateUser = repositoryUtilizatori::update;
        var u1 = repositoryUtilizatori.findOne(idUserSender).get();
        var u2 = repositoryUtilizatori.findOne(idUserRecv).get();
        u1.addFriend(u2);
        u2.addFriend(u1);
        updateUser.accept(u1);
        updateUser.accept(u2);
        this.notifyAllObservers(new ServiceChangeEvent(ChangeEventType.FRIENDS, idUserRecv, idUserSender));
    }

    public void refuseCererePrietenie(Long idUserSender, Long idUserRecv) {
        var cerereMomentanaOpt = this.repositoryCereriPrietenii.findOne(new Tuplu<>(idUserSender, idUserRecv));
        if (cerereMomentanaOpt.isEmpty()) {
            throw new ServiceException("Nu exista1");
        }
        var cerereMomentan = cerereMomentanaOpt.get();
        cerereMomentan.setRefused();
        var entitatea = this.repositoryCereriPrietenii.update(cerereMomentan);
        if (entitatea.isPresent()) {
            throw new ServiceException("Ceva nu a mers bine");
        }
        this.notifyAllObservers(new ServiceChangeEvent(ChangeEventType.FRIENDS, idUserRecv, idUserSender));
    }

    public Optional<FriendshipRequest> getRelatieBetween(Long idUser1, Long idUser2) {
        return this.repositoryCereriPrietenii.findOne(new Tuplu<>(idUser1, idUser2));
    }


    public void updateUser(Long idUtilizator, String numeNou, String prenumeNou) {
        var utilizatorNou = new User(prenumeNou, numeNou);
        utilizatorNou.setId(idUtilizator);
        validatorUtilizator.validate(utilizatorNou);
        var response = repositoryUtilizatori.update(utilizatorNou);
        if (response.isPresent()) {
            throw new ServiceException("Utilizator existent!");
        }
        this.notifyAllObservers(new ServiceChangeEvent());
    }

    /**
     * getter pentru toate relatiile de prietenie
     *
     * @return toate relatiile de prietenie
     */
    public Iterable<Friendship> getAllPrietenii() {
        return repositoryPrietenii.findAll();
    }

    /**
     * functie de gasire a unui utilizator dupa id
     *
     * @param idUtilizator id-ul userului
     * @return un Optional cu userul...poate fi empty daca nu a fost gasit userul
     */
    public Optional<User> findOne(Long idUtilizator) {
        return repositoryUtilizatori.findOne(idUtilizator);
    }

    /**
     * functie de gasire a unei prietenii dupa id
     *
     * @param idPrietenie id-ul prieteniei ca tuplu
     * @return un Optional cu prietenia...poate fi empty daca nu a fost gasit prietenia
     */
    public Optional<Friendship> findOne(Tuplu<Long, Long> idPrietenie) {
        return repositoryPrietenii.findOne(idPrietenie);
    }

    /**
     * 0
     * functie de stergere a unei relatii de prietenie
     *
     * @param idPrietenie id-ul relatiei
     * @throws ServiceException daca nu exista prietenia
     */
    public void deletePrietenie(Tuplu<Long, Long> idPrietenie) {
        var response = repositoryPrietenii.delete(idPrietenie);
        if (response.isEmpty()) {
            throw new ServiceException("Nu exista relatia de prietenie!");
        }
        var u1 = repositoryUtilizatori.findOne(idPrietenie.getLeft()).get();
        var u2 = repositoryUtilizatori.findOne(idPrietenie.getRight()).get();
        u1.deleteFriend(u2);
        u2.deleteFriend(u1);
        repositoryUtilizatori.update(u1);
        repositoryUtilizatori.update(u2);
        this.notifyAllObservers(new ServiceChangeEvent());
    }

    /**
     * numarul de utilziatori din repo
     *
     * @return nr de utilizatori
     */
    public int sizeRepositoryUtilizatori() {
        return repositoryUtilizatori.size();
    }

    /**
     * numarul de relatii de prieteneie din repo
     *
     * @return nr de prietenii
     */
    public int sizeRepositoryPrietenii() {
        return repositoryPrietenii.size();
    }

    /**
     * return numarul de comunitati  - de componente conexe
     *
     * @return numarul de comunitati
     */
    public int numarComunitati() {
        var rezultatDFS = DFS();
        return rezultatDFS.size();
    }

    /**
     * returneaza cea mai sociabila comunitate
     *
     * @return lista cu cei mai sociabili useri
     */
    public Iterable<User> ceaMaiSociabilaComunitate() {
        var lista = cautaComunitatea();
        var listaFinala = new ArrayList<User>();
        lista.forEach(x -> {
            var user = findOne(x).get();
            listaFinala.add(user);
        });
        return listaFinala;
    }

    /**
     * cauta o comunitate pe baza rezultatului DFS
     *
     * @return
     */
    private ArrayList<Long> cautaComunitatea() {
        var rezultatDFS = DFS();
        var comunitate = new ArrayList<Long>();
        int lungimeMaxima = 0;
        for (var componentaConexa : rezultatDFS) {
            int nrLegaturi = 0;
            for (var nod : componentaConexa) {
                var u1 = repositoryUtilizatori.findOne(nod).get();
                nrLegaturi += u1.getFriends().size();
            }
            nrLegaturi /= 2;
            if (nrLegaturi > lungimeMaxima) {
                lungimeMaxima = nrLegaturi;
                comunitate = componentaConexa;
            }
        }
        return comunitate;

    }

    /**
     * algoritmul de dfs
     *
     * @return
     */
    private ArrayList<ArrayList<Long>> DFS() {
        var listaAdiacenta = new HashMap<Long, Vector<Long>>();
        repositoryUtilizatori.findAll().forEach(x -> {
            listaAdiacenta.put(x.getId(), new Vector<>());
            x.getFriends().forEach(y -> {
                listaAdiacenta.get(x.getId()).add(y.getId());
            });
        });
        var dfs = new DFS(listaAdiacenta);
        return dfs.mainAlgorithm();
    }

    public List<FriendshipDTO> relatiiDePrietenie(Long idUser) {
        List<FriendshipDTO> listaPrieteni = new ArrayList<>();
        var optionalUtilizator = findOne(idUser);
        if (optionalUtilizator.isEmpty()) {
            return listaPrieteni;
        }
        var listaInitiala = optionalUtilizator.get().getFriends();
        listaInitiala.forEach(x -> {
            var relatie = repositoryPrietenii.findOne(new Tuplu<>(idUser, x.getId())).get();
            var prietenieDTIO = new FriendshipDTO(x.getLastName(), x.getFirstName(), relatie.getDateCreated());
            prietenieDTIO.setId1(idUser);
            prietenieDTIO.setId2(x.getId());
            listaPrieteni.add(prietenieDTIO);
        });
        return listaPrieteni;
    }

    public HashMap<Integer, List<User>> cereriDePrietenie(Long idUser) {
        HashMap<Integer, List<User>> returnDict = new HashMap<>();
        List<User> listaPrieteni = new ArrayList<>();
        List<User> listaUtilizatoriCereri = new ArrayList<>();

        var optionalUtilizator = findOne(idUser);
        if (optionalUtilizator.isEmpty()) {
            return returnDict;
        }
        var listaInitiala = repositoryCereriPrietenii.findCereriUser(idUser);
        listaInitiala.forEach(x -> {
            User user;
            if (x.getId().getLeft().equals(idUser)) {
                user = repositoryUtilizatori.findOne(x.getId().getRight()).get();
            } else {
                user = repositoryUtilizatori.findOne(x.getId().getLeft()).get();
            }
            if (x.getStatus() == FriendshipRequest.ACCPETED) {
                listaPrieteni.add(user);
            } else {
                listaUtilizatoriCereri.add(user);
            }
        });
        returnDict.put(1, listaPrieteni);
        returnDict.put(2, listaUtilizatoriCereri);
        return returnDict;
    }

    public List<FriendshipDTO> relatiiDePrietenieLuna(Long idUser, Integer luna) {
        Predicate<FriendshipDTO> prietenieDinLuna = friendshipDTO -> friendshipDTO.getFriendsFrom().getMonth().equals(Month.of(luna));
        var listaPrieteni = relatiiDePrietenie(idUser);
        return listaPrieteni.stream()
                .filter(prietenieDinLuna)
                .collect(Collectors.toList());

    }


    /**
     * @param username - numele de utilisator pe care l cauta (e unique in DB)
     * @param hasedPassword - parola la care vom da match in DB
     *  Cautam sa vedem daca un user are un account(aka este in DB tabelul utilizatori)
     * @return (1) present daca datele coresund
     * (2)empty altfel
     * */
    public User findOnesAccount(String username, String hasedPassword) {

        var result = repositoryUtilizatori.findOnesAccount(username, hasedPassword);
        if (result.isEmpty()) {
            throw new ServiceException("Numele de utilizator sau parola incorecte !!!");
        }
        return result.get();
    }
}
