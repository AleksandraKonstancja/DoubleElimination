package cs21120.assignment2017.solution;


import cs21120.assignment2017.CompetitionManager;
import cs21120.assignment2017.IManager;
import cs21120.assignment2017.Match;
import cs21120.assignment2017.NoNextMatchException;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Stack;


/**
 * @author alm82
 * @version 21.11.2017
 *
 * This class is a program managing double elimination for competitions. Two teams compete with each other
 * and a loser team is placed in a second pool getting another chance. If a team loses for a second time,
 * it is eliminated completely. At the end winners from first and second pool compete and the winner is
 * a winner of the whole competition. The class is using classes from Java collection framework: ArrayList,
 * LinkedList and Stack.
 *
 *
 */

public class DoubleElimAlm82 implements IManager {

    /***
     * This is an support class of DoubleElimAlm82 responsible for keeping track of the program states
     * ( changes to winners and losers pools after each match ).
     */
    private class State{

        private LinkedList<String> Winners = new LinkedList<>();
        private LinkedList<String> Losers = new LinkedList<>();

        private LinkedList<String> getWinners(){
            return Winners;
        }
        private LinkedList<String> getLosers(){
            return Losers;
        }
        private void setWinners(LinkedList<String> list){
            Winners = list;
        }
        private void setLosers(LinkedList<String> list){
            Losers = list;
        }
        private void print(){
            System.out.print("Winners: " + Winners + "\nLosers: " + Losers + "\n");
        }
    }

    /* set of two linked list ( implementation of queues ) keeping track of current winners and losers pools*/
   private LinkedList<String> WinnerQ = new LinkedList<>();
   private LinkedList<String> LoserQ = new LinkedList<>();

   /* set of two stack keeping track of previous program states and states that have been undone*/
   private Stack<State> History = new Stack<>();
   private Stack<State> RedoStack = new Stack<>();

   /* set of two Strings keeping track of current match players */
   private String Player1;
   private String Player2;

   /* winner of the current competition - this is not a final winner of the competition and it can be in winners pool
     just as well as losers pool */
   private String Winner;


    /**
     * The main method using CompetitionManager main method to run the program using my implementation
     * of double elimination and given text file list.txt.
     * @param args ( name of IManager class and text file with teams list )
     * @throws FileNotFoundException if the file cannot be found.
     */
    public static void main(String[] args) throws FileNotFoundException{
        String[] Names = {"cs21120.assignment2017.DoubleElimAlm82", "list.txt"};
        CompetitionManager.main(Names);
    }

    /**
     * Sets the players to use in competition - at the beginning all players are in the winners pool.
     * @param players array with players or teams saved from text file in the CompetitionManager
     */
    @Override
    public void setPlayers(ArrayList<String> players) {
        for ( String Player : players) {
            WinnerQ.add(Player);
        }
    }

    /**
     * Returns true if next competition is possible: either one of the pools still has 2 or more teams
     * or both pools have one team each.
     * @return returns true if there are more matches to play and false if the competition is over
     */
    @Override
    public boolean hasNextMatch() {

        if (WinnerQ.size() >= 2 || LoserQ.size() >=2 || (WinnerQ.size() == 1 && LoserQ.size() ==1) )
            return true;
        else
            return false;
    }

    /**
     * Saves the current state of the competition ( winners and losers pool ) and adds it to the top of a stack.
     * Creates two temporary Linked Lists and fills them with players from current winners and losers pools. Then
     * both lists are added to a state and the state is pushed into a stack.
     * @param stack - either History stack or Stack of undone operations
     */
    public void saveLastState(Stack stack){

        State LastState = new State();

        LinkedList<String> temp = new LinkedList();
        LinkedList<String> temp2 = new LinkedList();

        temp.addAll(WinnerQ);
        LastState.setWinners(temp);

        temp2.addAll(LoserQ);
        LastState.setLosers(temp2);

        stack.push(LastState);
    }

    /**
     * Sets the players for the next match based on size of each pool. If there is one team in each pool one player
     * is taken from each pool. Otherwise if there are more teams in the winners pool both players are taken from
     * the winner pool and if there are more teams ( or equal number other than 1 ) in losers pool both players
     * are taken from losers pool.
     * @return returns the next match with previously chosen players
     * @throws NoNextMatchException if the competition is over
     */
    @Override
    public Match nextMatch() throws NoNextMatchException {

        /* Player1 has to be removed from the list to reach next player, but then it is added back
         at its original position so that the state of the competition does not change.  */

        if (WinnerQ.size() > LoserQ.size()) {
            Player1 = WinnerQ.remove();
            Player2 = WinnerQ.peek();
            WinnerQ.addFirst(Player1);
        }
        else if ( WinnerQ.size() == 1 && LoserQ.size() == 1){
           Player1 = WinnerQ.peek();
           Player2 = LoserQ.peek();
        }
        else if ( LoserQ.size() >= WinnerQ.size()){
            Player1 = LoserQ.remove();
            Player2 = LoserQ.peek();
            LoserQ.addFirst(Player1);
        }

        return new Match(Player1, Player2);
    }

    /**
     * Sets the winner and loser of the competition. If both pools have one team each, teams are removed.
     * If winners pool has more teams ( match consisted of both players from winners pool ) winner of the competition
     * is added at the end of winners pool and loser is added at the end of losers pool. If losers pool had more
     * or the same number other than 1 ( match consisted of both players from losers pool ) winner of the competition
     * is added at the end of losers pool and loser is removed completely.
     * @param player1 true indicates the first player wins, otherwise player 2
     */
    @Override
    public void setMatchWinner(boolean player1) {

        /* before any changes are made, last state of the competition is saved in the history */
        saveLastState(History);

        String Loser;   // loser of current competition

        /* sets the loser and winner based on provided score */
        if (player1) {
            Winner = Player1;
            Loser = Player2;
        }
        else {
            Winner = Player2;
            Loser = Player1;
        }

        /* based on pool that players where taken from and the match result, puts teams at the end of appropriate
        pool or removes them completely from the competition */
        if ( LoserQ.size() == 1 && WinnerQ.size() == 1){
            WinnerQ.remove();
            LoserQ.remove();
        }
        else if ( WinnerQ.size() > LoserQ.size()){
            WinnerQ.remove(Loser);
            WinnerQ.remove(Winner);
            WinnerQ.add(Winner);
            LoserQ.add(Loser);
        }

        else if ( LoserQ.size() >= WinnerQ.size() ){
            LoserQ.remove(Winner);
            LoserQ.add(Winner);
            LoserQ.remove(Loser);
        }

        /* when new score is given and RedoStack is not empty, it is cleared so that redo has no effect until next undo */
        if (!RedoStack.isEmpty())
            RedoStack.clear();
    }

    /**
     * Gets the name of the player/team that finished in first place.
     * @return returns winner of the final competition or null if competition has not finished
     */
    @Override
    public String getWinner() {
       if (this.hasNextMatch())
           return null;
       else
           return Winner;
    }

    /**
     * Undo last competition scores. Brings back competition state to last saved state, removes that state
     * from the History stack and adds it to RedoStack.
     */
    @Override
    public void undo() {

        saveLastState(RedoStack);

        WinnerQ = History.peek().getWinners();
        LoserQ = History.pop().getLosers();

    }

    /**
     * Redo a previously undone match entry. Brings back last saved state from the stack containing undone operations
     * and removes it from the RedoStack and adds it to History.
     */
    @Override
    public void redo() {

        saveLastState(History);

        WinnerQ = RedoStack.peek().getWinners();
        LoserQ = RedoStack.pop().getLosers();

    }

    /**
     * Checks if the operation can be undone - if the program just started and no scores have been given yet
     * ( when the History is empty ) it cannot be undone.
     * @return true if the operation can be undone or false if it cannot
     */
    @Override
    public boolean canUndo() {

        if (History.isEmpty())
            return false;
        else
            return true;
    }

    /**
     * Checks if the operation can be redone. It cannot in two cases: either competition has finished and the final
     * winner has been set, or the number of undone operations is equal to 0 ( RedoStack is empty ).
     * @return true if the operation can be redone or false if it cannot
     */
    @Override
    public boolean canRedo() {

        if(!(WinnerQ.size() == 1 && LoserQ.size() == 1) && !RedoStack.isEmpty())
            return true;
        else
            return false;
    }
}
