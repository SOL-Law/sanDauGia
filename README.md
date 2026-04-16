```mermaid
classDiagram
    %% HỆ THỐNG MẠNG VÀ UI
    class MainUI {
        -PrintWriter out
        -Gson gson
        +main(String[] args)
    }
    class AuctionUI {
        -PrintWriter out
        -String userRole
        +updateAuctionInfo(String data)
        +updateTimer(int time)
    }
    class AuctionServer {
        +List~ClientHandler~ activeClients
        +boolean isAuctionRunning
        +startAuctionTimer(int seconds)
        +broadcast(String msg)
    }
    class ClientHandler {
        -Socket socket
        -String loggedInUser
        +run()
    }
    
    %% HỆ THỐNG QUẢN LÝ (DESIGN PATTERN)
    class AuctionManager {
        -static AuctionManager instance
        -Map~Integer, BidInfo~ items
        +getInstance() AuctionManager
        +placeBid(String item, int price, String user)
    }

    %% HỆ THỐNG HƯỚNG ĐỐI TƯỢNG (OOP MODEL)
    class Entity {
        <<abstract>>
        #int id
        +getId() int
        +equals(Object obj) boolean
    }
    class User {
        -String username
        -String password
        -double balance
    }
    class Item {
        <<abstract>>
        -String name
        -double startPrice
        -double currentPrice
        +displayItemDetails()*
    }
    class Electronics {
        -String brand
        -int warrantyMonths
        +displayItemDetails()
    }
    class Art {
        -String author
        -String material
        +displayItemDetails()
    }
    class Vehicle {
        -String vehicleType
        -String engine
        +displayItemDetails()
    }
    class Auction {
        -Item item
        -User highestBidder
        -boolean isActive
        +placeBid(User bidder, double amount)
    }
    class BidTransaction {
        -Auction auction
        -User bidder
        -double bidAmount
    }

    %% QUAN HỆ KẾ THỪA
    Entity <|-- User
    Entity <|-- Item
    Entity <|-- Auction
    Entity <|-- BidTransaction
    Item <|-- Electronics
    Item <|-- Art
    Item <|-- Vehicle

    %% QUAN HỆ TƯƠNG TÁC
    MainUI --> AuctionUI : Mở giao diện
    AuctionServer "1" *-- "n" ClientHandler : Quản lý
    ClientHandler --> AuctionManager : Gọi xử lý
    Auction "1" o-- "1" Item : Chứa
    BidTransaction "n" --> "1" Auction : Thuộc về
