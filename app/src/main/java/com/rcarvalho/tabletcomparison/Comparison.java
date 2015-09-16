package com.rcarvalho.tabletcomparison;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
This class handles comparisons. It holds an array of Tablets and can indicate which of the tablets
 has the best specs
 */
public class Comparison
{
    //The tablets to compare
    private ArrayList<Tablet> tablets = new ArrayList<>(); /* Holds all available tablets */

    //The overall ranking list
    private List<Map.Entry<Tablet, Integer>> tabletRanking;

    //Reference tablet, will hold all the best specs available in this comparison
    private Tablet referenceTablet;

    //Number of specifications that have been compared
    int numSpecs;

/*************************************** Constructors *********************************************/

    public Comparison(Tablet[] tablets)
    //Constructor: to use this class, you need to provide it with at least 2 tablets
    {
        if (tablets.length < 2)
        {
            //Todo: add error handling
        }
        else
        {
            //Add the tablets to the local array
            for (Tablet tablet : tablets)
            {
                this.tablets.add(tablet);
            }
            //Create a reference tablet
            setBestSpecs();

            //Determine the overall ranking
            setTabletRankingRanking();

        }
    }

/****************************************** Getters ***********************************************/

    public int getNumSpecs()
    //Returns the number of specs contained in this comparison
    {
        return numSpecs;
    }

    public Tablet getReferenceTablet()
    //Returns a reference tablet, which holds the best of all the specs in this comparison
    {
        return referenceTablet;
    }

    public List<Map.Entry<Tablet, Integer>> getTabletRanking()
    //Contains a sorted map of all the tablets in this comparison, by the best specs
    {
        return tabletRanking;
    }

/****************************************** Setters ***********************************************/

    public void setBestSpecs()
    //This method determines the best value within all the specs
    {
        //Setup the reference tablet
        referenceTablet = new Tablet();

        //Find out how many specs we're dealing with, based on the first tablet we will compare
        Tablet firstTablet = tablets.get(0);
        Specification[] firstTabletSpecs = firstTablet.getSpecs();
        numSpecs = firstTabletSpecs.length;

        //Setup a multidimensional array, which will hold all the specs of all the tablets
        Specification[][] allSpecs = new Specification[numSpecs][tablets.size()];

        //Fill the array with all the specifications we have in this comparison
        for (int i =0 ; i < numSpecs ; i++)
        {
            int tabletNum = 0;
            for (Tablet tablet : tablets)
            {
                Specification[] tabletSpecs = tablet.getSpecs();
                allSpecs[i][tabletNum] = tabletSpecs[i];
                tabletNum++;
            }
        }

        //Now loop through the array to store the best of all specifications
        int specNum = 0;
        for (Specification[] specification : allSpecs)
        {
            // Create a hash map to sort specifications by value
            HashMap<Specification, Float> rankingList = new HashMap<>();

            int tabletNum = 0;
            for (Tablet tablet : tablets)
            {
                rankingList.put(specification[tabletNum],specification[tabletNum].getValue());
                tabletNum++;
            }

            //Sort the result by score
            Set<Map.Entry<Specification, Float>> set = rankingList.entrySet();
            ArrayList<Map.Entry<Specification, Float>> specRanking =
                    new ArrayList<Map.Entry<Specification, Float>>(set);
            Collections.sort(specRanking, new Comparator<Map.Entry<Specification, Float>>() {
                public int compare(Map.Entry<Specification, Float> o1, Map.Entry<Specification, Float> o2) {
                    return (o2.getValue()).compareTo(o1.getValue());
                }
            });

            //Now add the best of specification to the reference tablet
            if (specification[0].getRankingHighToLow())
            {
                //Set the first specification in the list as reference spec
                referenceTablet.setSpec(specRanking.get(0).getKey());
            }
            else
            {
                //Set the last specification in the list as reference spec
                int size = specRanking.size();
                referenceTablet.setSpec(specRanking.get(size-1).getKey());
            }


        }

    }

    public void setTabletRankingRanking()
    //This method scores each tablet on all scorable specs and creates a sorted list of the tablets
    // based on score
    {
        // Create a hash map
        HashMap<Tablet, Integer> rankingList = new HashMap<>();
        int tabletNum = 0;

        //For each tablet, add points if they match the best specifications
        for (Tablet tablet: tablets)
        {
            int score = 0;
            Specification[] tabletSpecs = tablet.getSpecs();
            Specification[] referenceSpecs = referenceTablet.getSpecs();

            //Calculate score
            for (int i = 0 ; i < numSpecs ; i++)
            {
                if (tabletSpecs[i].getRankingHighToLow()) {
                    if (tabletSpecs[i].getScorable() && tabletSpecs[i].getValue() >=
                            referenceSpecs[i].getValue())
                    {
                        score++;
                    }
                }
                else
                {
                    if (tabletSpecs[i].getScorable() && tabletSpecs[i].getValue() <=
                            referenceSpecs[i].getValue())
                    {
                        score++;
                    }
                }
            }

            rankingList.put(tablet, score);
            tabletNum++;
        }

        //Sort the result by score
        Set<Map.Entry<Tablet, Integer>> set = rankingList.entrySet();
        tabletRanking = new ArrayList<Map.Entry<Tablet, Integer>>(set);
        Collections.sort(tabletRanking, new Comparator<Map.Entry<Tablet, Integer>>() {
            public int compare(Map.Entry<Tablet, Integer> o1, Map.Entry<Tablet, Integer> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });
    }
}
