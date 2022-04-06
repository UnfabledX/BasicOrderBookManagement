import java.io.*;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * bid (there are people willing to buy at this price)
 * ask (people are willing to sell at this price)
 * The best bid is always lower than the best ask.
 * <p>
 * Updates to the limit order book in the following format:
 * u,<price>,<size>,bid - set bid size at <price> to <size> (size shares in total are now being offered at price)
 * u,<price>,<size>,ask - set ask size at <price> to <size>
 * <p>
 * Queries in the following format:
 * q,best_bid - print best bid price (max price) and size
 * q,best_ask - print best ask price (min price) and size
 * q,size,<price> - print size at specified price (bid, ask or spread).
 * <p>
 * And market orders in the following format:
 * o,buy,<size> - removes <size> shares out of asks, most cheap ones.
 * o,sell,<size> - removes <size> shares out of bids, most expensive ones
 * <p>
 * Input values range:
 * Price - 1...10E9     int max - 2,147,483,647
 * Size - 0...10E8
 */
public class Solution {

    private static final String DEST_FILE = "output.txt";
    private static final String SOURCE_FILE = "input.txt";
    private static final String U_BID = "bid";
    private static final String U_ASK = "ask";
    private static final String Q_BEST_BID = "best_bid";
    private static final String Q_BEST_ASK = "best_ask";
    private static final String Q_SIZE = "size";
    private static final String O_BUY = "buy";
    private static final String O_SELL = "sell";

    public static void main(String[] args) {
        String[] lineArray;
        SortedMap<Integer, Integer> bidMap = new TreeMap<>(); // Map<Price, Size> bidMap
        SortedMap<Integer, Integer> askMap = new TreeMap<>(); // Map<Price, Size> askMap

        try (BufferedReader reader = new BufferedReader(new FileReader(SOURCE_FILE));
             BufferedWriter writer = new BufferedWriter(new FileWriter(DEST_FILE, false))) {
            StringBuilder builder = new StringBuilder();
            while (reader.ready()) {
                lineArray = reader.readLine().split(",");
                switch (lineArray[0]) {
                    case "u":
                        updateOrders(lineArray, U_BID, bidMap);
                        updateOrders(lineArray, U_ASK, askMap);
                        break;

                    case "q":
                        switch (lineArray[1]) {
                            case Q_BEST_BID:
                                if (!bidMap.isEmpty()) {
                                    int maxPriceBid = bidMap.lastKey();
                                    int sizeOfMaxPriceBid = bidMap.get(maxPriceBid);
                                    builder.append(String.format("%d,%d%n", maxPriceBid, sizeOfMaxPriceBid));
                                }
                                break;
                            case Q_BEST_ASK:
                                if (!askMap.isEmpty()) {
                                    int minPriceAsk = askMap.firstKey();
                                    int sizeOfMinPriceAsk = askMap.get(minPriceAsk);
                                    builder.append(String.format("%d,%d%n", minPriceAsk, sizeOfMinPriceAsk));
                                }
                                break;
                            case Q_SIZE:
                                int q_price = Integer.parseInt(lineArray[2]);
                                if (bidMap.containsKey(q_price)) {
                                    builder.append(String.format("%d%n", bidMap.get(q_price)));
                                } else if (askMap.containsKey(q_price)) {
                                    builder.append(String.format("%d%n", askMap.get(q_price)));
                                }
                                break;
                        }
                        break;

                    case "o":
                        switch (lineArray[1]) {
                            case O_BUY:
                                int minPriceAsk = askMap.firstKey();
                                int sizeOfMinPriceAsk = askMap.get(minPriceAsk);
                                int sizeToBuy = Integer.parseInt(lineArray[2]);
                                if (sizeToBuy < sizeOfMinPriceAsk) {
                                    askMap.put(minPriceAsk, sizeOfMinPriceAsk - sizeToBuy);
                                } else {
                                    while (sizeToBuy >= sizeOfMinPriceAsk) {
                                        sizeToBuy -= sizeOfMinPriceAsk;
                                        askMap.remove(minPriceAsk);
                                        if (!askMap.isEmpty()) {
                                            minPriceAsk = askMap.firstKey();
                                            sizeOfMinPriceAsk = askMap.get(minPriceAsk);
                                        } else break;
                                    }
                                    if (!askMap.isEmpty()) {
                                        askMap.put(minPriceAsk, sizeOfMinPriceAsk - sizeToBuy);
                                    }
                                }
                                break;
                            case O_SELL:
                                int maxPriceBid = bidMap.lastKey();
                                int sizeOfMaxPriceBid = bidMap.get(maxPriceBid);
                                int sizeToSell = Integer.parseInt(lineArray[2]);
                                if (sizeToSell < sizeOfMaxPriceBid) {
                                    bidMap.put(maxPriceBid, sizeOfMaxPriceBid - sizeToSell);
                                } else {
                                    while (sizeToSell >= sizeOfMaxPriceBid) {
                                        sizeToSell -= sizeOfMaxPriceBid;
                                        bidMap.remove(maxPriceBid);
                                        if (!bidMap.isEmpty()) {
                                            maxPriceBid = bidMap.lastKey();
                                            sizeOfMaxPriceBid = bidMap.get(maxPriceBid);
                                        } else break;
                                    }
                                    if (!bidMap.isEmpty()) {
                                        bidMap.put(maxPriceBid, sizeOfMaxPriceBid - sizeToSell);
                                    }
                                }
                                break;
                        }
                        break;
                }
            }
            writer.write(builder.toString().strip());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void updateOrders(String[] lineArray, String BidOrAsk, SortedMap<Integer, Integer> bidOrAskMap) {
        int price = Integer.parseInt(lineArray[1]);
        int size = Integer.parseInt(lineArray[2]);
        if (lineArray[3].equals(BidOrAsk)) {
            if (bidOrAskMap.containsKey(price)) {
                int oldSize = bidOrAskMap.get(price);
                bidOrAskMap.put(price, size + oldSize);
            } else {
                bidOrAskMap.put(price, size);
            }
        }
    }
}
