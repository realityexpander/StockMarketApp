# StockMarketApp
Stock Tracking App using SOLID architecture, uses Hilt, Room, Compose, Coroutines, Retrofit, www.alphavantage.co API

[<img src="https://user-images.githubusercontent.com/5157474/171551314-c0e1dd15-310a-4808-9c25-1be2749a0980.png" width="350"/>](https://user-images.githubusercontent.com/5157474/171551314-c0e1dd15-310a-4808-9c25-1be2749a0980.png)
[<img src="https://user-images.githubusercontent.com/5157474/171551338-c5dd5906-d6cf-4316-b2ef-5273c0632dff.png" width="350"/>](https://user-images.githubusercontent.com/5157474/171551338-c5dd5906-d6cf-4316-b2ef-5273c0632dff.png)


- Allows users to search for stock by name or stock symbol
- Displays custom graph
- Search is interruptable

---- Tech used ----
- List of stocks is cached locally in a Room database
- Uses custom query for Room database
- Shows proper use of Dagger-Hilt & SOLID architecture
- Uses compose for view layer, ViewModel and mutableState to send UI events
- Uses Resource sealed class to handle errors and messaging
- Custom CSV parsers using OpenCSV for Company Listings and Intraday price info
- Custom canvas drawing using interop to android XML text rendering
- Graph uses Bezier curves

To install the Apk:

1. Open this link on your Android device:
   https://github.com/realityexpander/StockMarketApp/blob/master/StockMarketApp_1.0.apk
2. Tap the "skewer" menu and tap the "download"

   [![](https://user-images.githubusercontent.com/5157474/147434050-57102a30-af32-46ed-a90b-d94e0c4a4f35.jpg)]()
3. Allow the file to download (DO NOT click "show details")
4. After the file is downloaded, click "OK" to install
5. Click "OK" to install
6. Click "OK" to launch

If you have developer options turned on, you may need to turn off "USB Debugging" if the "Waiting for debugger" dialog is displayed.
