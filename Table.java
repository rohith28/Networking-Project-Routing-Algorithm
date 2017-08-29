 /**  Name: Rohith Kumar Uppala
 *    Filename: Table.java
 *    Date: 3/23/2017
 *    Course: COMP 594
 *    Description: Routing Table
 */

class Table 
{
    int cost = 0;
    int outPort = 0;
    int destPort = 0;

    Table(int first, int second, int third) {
        cost = first;
        outPort = second;
        destPort = third;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int first) {
        cost = first;
    }

    public int getOutPort() {
        return outPort;
    }

    public void setOutPort(int second) {
        outPort = second;
    }
    
    public int getDestPort() {
        return destPort;
    }

    public void setDestPort(int second) {
        destPort = second;
    }

}