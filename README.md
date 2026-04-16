classDiagram
    class MainUI {
        -PrintWriter out
        -BufferedReader in
        -Gson gson
        -AuctionUI auctionUI
        +main(String[] args)
    }

    class AuctionUI {
        -PrintWriter out
        -Gson gson
        -String userRole
        -JButton bidButton
        -JButton startButton
        +updateAuctionInfo(String data)
        +updateTimer(int timeLeft)
        +auctionEnded(String resultMessage)
        +startNewSession(String message)
    }

    class Request {
        -String action
        -String payload
        +getAction() String
        +getPayload() String
    }

    class AuctionServer {
        +List~ClientHandler~ activeClients
        +boolean isAuctionRunning
        +main(String[] args)
        +startAuctionTimer(int seconds)
        +broadcast(String jsonMessage)
    }

    class ClientHandler {
        -Socket socket
        -String loggedInUser
        -String userRole
        +run()
        +sendMessage(String msg)
    }

    class AuctionManager {
        -Map~Integer, BidInfo~ items
        -boolean isRunning
        -static AuctionManager instance
        +getInstance() AuctionManager
        +placeBid(String itemName, int price, String user)
        +startNewSession()
        +endAuction()
        +getAllItems() String
    }

    class UserDao {
        +login(String username, String password) String
        +register(String username, String password) boolean
    }

    MainUI --> AuctionUI : Khởi tạo khi Login thành công
    MainUI ..> Request : Đóng gói JSON
    AuctionUI ..> Request : Đóng gói JSON
    
    AuctionServer "1" *-- "n" ClientHandler : Quản lý đa luồng
    ClientHandler --> AuctionManager : Gọi xử lý đấu giá
    ClientHandler --> UserDao : Kiểm tra tài khoản
    AuctionServer --> AuctionManager : Điều khiển phiên (Timer)
