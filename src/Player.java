import jade.core.AID;

//Clase con la información de cada jugador
public class Player {

    private final AID AID; // Identificador único de JADE
    private int ID; // Identificador asignado por el agente principal
    private String name; // Nombre del agente especificado en la ejecución del programa
    private String type; // Tipo de agente
    private int wins; // Número de victorias
    private int ties; // Número de empates
    private int losses; // Número de derrotas
    private double money; // Dinero acumulado
    private int roundMoney; // Dinero durante la ronda actual
    private double stocks; // Cantidad de stocks

    // Constructor
    public Player(AID AID, String name, String type) {
        this.AID = AID;
        this.ID = -1; // Asignado por el agente principal una vez iniciado el torneo
        this.name = name;
        this.type = type;
        this.wins = 0;
        this.losses = 0;
        this.ties = 0;
        this.money = 0.0;
        this.roundMoney = 0;
        this.stocks = 0.0;
    }

    // Getters
    public AID getAID() {
        return AID;
    }

    public int getID() {
        return ID;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getWins() {
        return wins;
    }

    public int getTies() {
        return ties;
    }

    public int getLosses() {
        return losses;
    }

    public double getMoney() {
        return money;
    }

    public int getRoundMoney() {
        return roundMoney;
    }

    public double getStocks() {
        return stocks;
    }

    // Setters
    public void setID(int ID) {
        this.ID = ID;
    }

    public void addWin() {
        this.wins++;
    }

    public void addTie() {
        this.ties++;
    }

    public void addLoss() {
        this.losses++;
    }

    public void setMoney(double money) {
        this.money = MainAgent.round(money);
    }

    public void addMoney(double amount) {
        this.money = MainAgent.round(this.money + amount);
    }

    public void removeMoney(double amount) {
        this.money = MainAgent.round(this.money - amount);
    }

    public void setRoundMoney(int money) {
        this.roundMoney = money;
    }

    public void addRoundMoney(int amount) {
        this.roundMoney += amount;
    }

    public void setStocks(double stocks) {
        this.stocks = MainAgent.round(stocks);
    }

    public void addStocks(double amount) {
        this.stocks = MainAgent.round(this.stocks + amount);
    }

    public void removeStocks(double amount) {
        this.stocks = MainAgent.round(this.stocks - amount);
    }
}
