import com.sun.webkit.Utilities;
import javafx.util.Pair;

import java.io.*;
import java.util.*;

@SuppressWarnings("unchecked")
class Dijkstra {
    char start;
    char target;
    final Map<Character, Pair<Character, Integer>[]> vertices;
    final HashMap<Character, Integer> distancesMap;
    final HashMap<Character, Character> previousMap;
    final List<Character> visitedList;

    protected Dijkstra(char start, char target, Map<Character, Pair<Character, Integer>[]> givenMap) {
        this.start = start;
        this.target = target;
        this.vertices = givenMap;
        int initialSize = this.vertices.size();
        visitedList = new ArrayList<>();
        distancesMap = new HashMap<>(initialSize);
        previousMap = new HashMap<>(initialSize);
        fillDistancesMap();
        fillPreviousMap();
    }

    private void fillDistancesMap() {
        Set<Character> verticesSet = vertices.keySet();
        for (Character c : verticesSet) {
            distancesMap.put(c, Integer.MAX_VALUE - 1);
        }
    }
    private void fillPreviousMap() {
        Set<Character> verticesSet = vertices.keySet();
        for (Character c : verticesSet) {
            previousMap.put(c, null);
        }
    }
    public static void main(String[] args) {
        Map<Character, Pair<Character, Integer>[]> vertices = new HashMap<>(7);
        vertices.put('A', new Pair[]{new Pair('C', 3), new Pair('F', 2)});
        vertices.put('B', new Pair[]{new Pair('G', 2), new Pair('F', 6), new Pair('E', 2), new Pair('D', 1)});
        vertices.put('C', new Pair[]{new Pair('F', 2), new Pair('E', 1), new Pair('D', 4), new Pair('A', 3)});
        vertices.put('D', new Pair[]{new Pair('C', 4), new Pair('B', 1)});
        vertices.put('E', new Pair[]{new Pair('C', 1), new Pair('F', 3), new Pair('B', 2)});
        vertices.put('F', new Pair[]{new Pair('C', 2), new Pair('E', 3), new Pair('B', 6), new Pair('G', 5), new Pair('A', 2)});
        vertices.put('G', new Pair[]{new Pair('F', 5), new Pair('B', 2)});

        Dijkstra finder = new Dijkstra('A','B',vertices);
        System.out.println(finder.findShortestPath());

        Map<Character, Pair<Character, Integer>[]> map2 = produceMap();
        Dijkstra generatedFinder = new Dijkstra('A','F', map2);
        System.out.println(generatedFinder.findShortestPath());
    }
    private int findShortestPath() {
        distancesMap.put(start, 0);
        visitedList.add(start);
        char currentLocation = start;
        while (target != currentLocation){
            Pair [] adjacentVertices = vertices.get(currentLocation);
            for(Pair vertexAsPair : adjacentVertices){
                char nextVertexAsChar = (char)vertexAsPair.getKey();
                if(visitedList.contains(nextVertexAsChar)) continue;
                int locationEstimate = distancesMap.get(nextVertexAsChar);
                int currentEstimate = (int)vertexAsPair.getValue() + distancesMap.get(currentLocation);
                if(locationEstimate>currentEstimate){
                    //update estimate
                    distancesMap.put(nextVertexAsChar,currentEstimate);
                    //point to previous node
                    previousMap.put(nextVertexAsChar,currentLocation);
                }
            }
            //choose smallest adjacent but not yet visited
            int minVal = Integer.MAX_VALUE;
            char minChar = ' ';
            for (Pair vertexAsPair : adjacentVertices) {
                char location = (char)vertexAsPair.getKey();
                int distance = distancesMap.get(location);
                if (!visitedList.contains(location) && distance < minVal) {
                    minVal = distance;
                    minChar = location;
                }
            }
            currentLocation = minChar;
            if(currentLocation == ' ') {
                System.err.println("Graph probably doesn't exist, attempting backtrace");
                try{
                    currentLocation = visitedList.get(visitedList.size()-2);
                }catch (IndexOutOfBoundsException indexExc){
                    System.err.println("Failed, exiting");
                    return -1;
                }
            }
            visitedList.add(currentLocation);
        }
        displayPath();
        return distancesMap.get(target);
    }

    private void displayPath() {
        char backtrace = target;
        List<Character> path = new ArrayList<>(visitedList.size());
        while(true){
            path.add(backtrace);
            if(previousMap.get(backtrace)==null){
                break;
            }
            backtrace = previousMap.get(backtrace);
        }
        Collections.reverse(path);
        String pathString = path.toString();
        pathString = pathString.substring(1,pathString.length()-1).replace(","," ->");
        System.out.println(pathString);
    }

    private static HashMap<Character,Pair<Character,Integer>[]> produceMap(){
        Scanner input = new Scanner(System.in);
        System.out.println("How many vertices: ");
        int howMany = input.nextInt();
        HashMap<Character,Pair<Character,Integer>[]> map = new HashMap<>(howMany);
        System.out.println("Type: 'next' to go to next vertex");
        String typed;

        for(int vertex = 1; vertex<=howMany; ++vertex){
            System.out.print("\n#" + vertex + ": ");
            System.out.print(" char vertex: ");
            char vertexChar = ' ';
            while(true){
                typed = input.nextLine();
                if(typed.length() == 1 && Character.isLetter(typed.charAt(0))){
                    vertexChar = typed.charAt(0);
                    break;
                }
            }
            System.out.println(" its neighbors [char int] or [int char]: ");
            List<Pair<Character,Integer>> neighbors = new ArrayList<>(10);
            while(!(typed = input.nextLine()).equalsIgnoreCase("next")){
                if(typed.length()<2){
                    System.err.println("Invalid");
                    continue;
                }

                char label = ' ';
                int dist = -1;
                boolean wasDistanceRead = false;
                char [] pairArr = typed.toCharArray();
                for(int i = 0; i<pairArr.length;++i){
                    if(Character.isLetter(pairArr[i]) && pairArr[i]!=vertexChar){
                        label = pairArr[i];
                    }else if(!wasDistanceRead && Character.isDigit(pairArr[i])){
                        Pair<Integer,Integer> result = readNum(pairArr,i);
                        wasDistanceRead = true;
                        i = result.getKey();
                        dist = result.getValue();
                    }
                }
                if(dist<0 || label==' ') {
                    System.err.println("Invalid");
                    continue;
                }
                Pair<Character,Integer> pair = new Pair<>(label,dist);
                neighbors.add(pair);
            }
            //convert to arr[]
            Pair [] arr = new Pair[neighbors.size()];
            for(int j = 0; j<arr.length; ++j){
                arr[j] = neighbors.get(j);
            }
            map.put(vertexChar,arr);
        }
        return map;
    }

    private static Pair<Integer,Integer> readNum(char[] pairArr, int from) {
        //lastIndex, number
        StringBuilder numberStr = new StringBuilder(String.valueOf(pairArr[from]));
        int i = from+1;
        for(;i<pairArr.length; ++i){
            if(Character.isDigit(pairArr[i])){
                numberStr.append(pairArr[i]);
            }else{
                break;
            }
        }
        return new Pair<>(i-1,Integer.parseInt(numberStr.toString()));
    }
    private static void saveGraph(String path, Map<Character,Pair<Character,Integer>[]> map){
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
            oos.writeObject(map);
            oos.close();
        } catch (IOException ioException) {
            System.err.println("Failure");
        }
    }
    private static Map<Character,Pair<Character,Integer>[]> readGraph(String path){
        Map<Character,Pair<Character,Integer>[]> map = null;
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path));
            Object obj = ois.readObject();
            map = (Map<Character, Pair<Character, Integer>[]>) obj;
            ois.close();
        }catch(IOException | ClassNotFoundException ioE){
            System.err.println("Failure");
        }
        return map;
    }

}
