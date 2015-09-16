package com.rcarvalho.tabletcomparison;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
This fragment shows a table comparing the specs of a given array of Tablets. It will first
 list the tablet(s) the Comparison object has found to be the best and offer users the opportunity
 to add these to favorites. Next, it lists all specs compared, indicated which of the tablets
 has the best of the specs listed.
 */
public class TabletComparisonFragment extends Fragment {

    //Public array to hold all the compared Tablets
    Tablet[] tablets;


    public TabletComparisonFragment()
    //Constructor: Not needed in this context
    {
    }

/******************************** Main Activity Methods *******************************************/

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    //When the activity is created, load up Intent data and show comparison
    {
        //Call superclass method
        super.onActivityCreated(savedInstanceState);

        //Get view components
        Button closeButton = (Button) getActivity().findViewById(R.id.comparison_close_button);

        //Set button to close the fragment
        closeButton.setOnClickListener
                //When someone taps the button, close the fragment and return intent data
                (
                        new View.OnClickListener()
                        {
                            public void onClick(View v)
                            {
                                closeFragmentWithOptionalReset();
                            }
                        }
                );

        //Get intent data
        Intent intent = getActivity().getIntent();
        Bundle bundle = intent.getExtras();

        if (bundle != null)
        //Receive the tablets the user wants to compare
        {
            Parcelable[]data = getActivity().getIntent().getParcelableArrayExtra("Tablets");
            ArrayList<Tablet> receivedTablets = new ArrayList<>();

            //Convert the parcel objects to Tablets
            for (Parcelable object : data)
            {
                Tablet tablet = (Tablet)object;
                receivedTablets.add(tablet);
            }

            //Add tablets to local array
            tablets = new Tablet[receivedTablets.size()];
            tablets = receivedTablets.toArray(tablets);
        }

        //Run comparison
        Comparison comparison = new Comparison(tablets);

        //Display results
        showRecommendation(comparison);
        showComparison(comparison);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tablet_comparison, container, false);
    }

/*************************************** Subroutines **********************************************/

    public void closeFragment(boolean resetSelections)
    //Closes the fragment and returns to the previous fragment with Intent data. Can be called
    // from the parent activity
    {

        //Prepare intent data
        Intent data = new Intent();
        data.putExtra("Tablets", tablets);

        if (resetSelections)
        {
            data.putExtra("Reset", true);
        }
        else
        {
            data.putExtra("Reset", false);
        }

        //Close activity and return intent data
        getActivity().setResult(Activity.RESULT_OK, data);
        getActivity().finish();
    }
    private void closeFragmentWithOptionalReset()
    /*
    This method asks the user if they want to reset any selected tablets for comparison
    */
    {
        //Show an alert dialog asking the user what they prefer
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
        builder.setMessage(getResources().getString(R.string.reset_comparison_msg))
                .setTitle(getResources().getString(R.string.reset_comparison_title))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.yes_text),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                closeFragment(true);
                            }
                        })

                .setNegativeButton(getResources().getString(R.string.no_text),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                closeFragment(false);
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();

    }

    public void showComparison(Comparison comparison)
    //Shows all the specifications of the selected tablets, with the tablet that has the best of a
    // given specification marked
    {

        //Get the specifications that need to be displayed
        int numSpecifications = comparison.getNumSpecs();
        Tablet referenceTablet = comparison.getReferenceTablet();

        //Variable to keep track of the tablet index
        int specNum = 0;

        for (int i = 0 ; i < numSpecifications; i++)
        //Loop through each available spec
        {
            Specification spec = referenceTablet.getSpecs()[i];

            //Find out if the specification should be displayed
            if (spec.getScorable())
            {
                //Find Tablelayout defined in main.xml
                TableLayout tl = (TableLayout) getActivity().findViewById(R.id.comparisonTable);
                int margin = (int) getResources().getDimension(R.dimen.normal_margin);
                int tableMargin = (int) getResources().getDimension(R.dimen.table_margin);

                //Create a new row in the table that will hold the section title
                TableRow row = new TableRow(this.getActivity());
                TableLayout.LayoutParams tableRowParams=
                        new TableLayout.LayoutParams
                                (TableLayout.LayoutParams.MATCH_PARENT,TableLayout.LayoutParams.
                                        MATCH_PARENT);
                tableRowParams.topMargin = tableMargin;
                tableRowParams.bottomMargin = tableMargin;
                tableRowParams.width = 0;
                tableRowParams.weight = 1;
                row.setLayoutParams(tableRowParams);

                //Create a new relative layout to hold views in the row
                RelativeLayout rowLayout = new RelativeLayout(this.getActivity());


                //Create a title textview within the row layout
                TextView title = new TextView(this.getActivity());

                int titleId = Helper.generateViewId();
                title.setId(titleId);
                rowLayout.addView(title);

                //Set the title text its layout
                title.setText(spec.getName());
                title.setTextAppearance(this.getActivity(), android.R.style.
                        TextAppearance_DeviceDefault_Medium);
                title.setTypeface(null, Typeface.BOLD);
                RelativeLayout.LayoutParams titleParams = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT);
                title.setLayoutParams(titleParams);

                //Add the layout to the row
                row.addView(rowLayout);

                //Align the title to the center
                title.setGravity(Gravity.CENTER);
                row.setGravity(Gravity.CENTER);

                //Add a background color
                row.setBackgroundColor(getResources().getColor(R.color.button_material_light));

                // Add row to TableLayout
                tl.addView(row, new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT,
                        TableLayout.LayoutParams.WRAP_CONTENT));

                //Now create rows for each tablet containing the specification information
                for (Tablet tablet : tablets)
                {
                    Specification[] tabletSpecs = tablet.getSpecs();
                    Specification tabletSpec = tabletSpecs[i];
                    float specValue = tabletSpec.getValue();
                    float referenceValue = spec.getValue();


                    row = new TableRow(this.getActivity());
                    tableRowParams=
                            new TableLayout.LayoutParams
                                    (TableLayout.LayoutParams.MATCH_PARENT,TableLayout.LayoutParams
                                            .MATCH_PARENT);
                    tableRowParams.topMargin = tableMargin;
                    tableRowParams.bottomMargin = tableMargin;
                    tableRowParams.width = 0;
                    tableRowParams.weight = 1;
                    row.setLayoutParams(tableRowParams);

                    //Create a new relative layout to hold views in the row
                    rowLayout = new RelativeLayout(this.getActivity());

                    //Create a new image within the row layout
                    ImageView image = new ImageView(this.getActivity());
                    int imageId = Helper.generateViewId();
                    image.setId(imageId);
                    rowLayout.addView(image);

                    //Set the image and set its layout
                    image.setImageResource(tablet.getImage());
                    image.setScaleType(ImageView.ScaleType.FIT_XY);
                    RelativeLayout.LayoutParams imageLayoutParams = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT,
                            RelativeLayout.LayoutParams.MATCH_PARENT);
                    imageLayoutParams.width = (int) getResources().getDimension(R.dimen.image_width);
                    imageLayoutParams.height = (int) getResources().getDimension(R.dimen.image_height);
                    imageLayoutParams.setMargins(0,0, margin, 0 );
                    imageLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    image.setLayoutParams(imageLayoutParams);

                    //Add the tablet name
                    TextView tabletName = new TextView(this.getActivity());
                    int tabletNameId = Helper.generateViewId();
                    tabletName.setId(tabletNameId);
                    rowLayout.addView(tabletName);

                    //Set the text its layout
                    tabletName.setText(tablet.getName());
                    tabletName.setTextAppearance(this.getActivity(), android.R.style.
                            TextAppearance_DeviceDefault_Medium);
                    RelativeLayout.LayoutParams tabletNameParams = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT,
                            RelativeLayout.LayoutParams.MATCH_PARENT);
                    tabletNameParams.addRule(RelativeLayout.RIGHT_OF, imageId);
                    tabletName.setLayoutParams(tabletNameParams);

                    //Create a specification textview within the row layout
                    TextView specificationView = new TextView(this.getActivity());
                    int specificationViewId = Helper.generateViewId();
                    specificationView.setId(specificationViewId);
                    rowLayout.addView(specificationView);

                    //Set the specification text its layout
                    String specText = tabletSpec.toString();
                    specificationView.setText(specText);
                    specificationView.setTextAppearance(this.getActivity(), android.R.style.
                            TextAppearance_DeviceDefault_Small);
                    RelativeLayout.LayoutParams specificationViewParams = new RelativeLayout.
                            LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT,
                            RelativeLayout.LayoutParams.MATCH_PARENT);
                    specificationViewParams.addRule(RelativeLayout.RIGHT_OF, imageId);
                    specificationViewParams.addRule(RelativeLayout.BELOW, tabletNameId);
                    specificationView.setLayoutParams(specificationViewParams);

                    //Find out if the specification is the highest in this comparison
                    if (specValue == referenceValue)
                    {
                        //Add a star
                        TextView bestBadge = new TextView(this.getActivity());
                        int bestBadgeId = Helper.generateViewId();
                        bestBadge.setId(bestBadgeId);
                        rowLayout.addView(bestBadge);

                        //Set the favorite indicator text and layout
                        bestBadge.setCompoundDrawablesWithIntrinsicBounds
                                (R.drawable.ic_action_important, 0, 0, 0);

                        RelativeLayout.LayoutParams bestBadgeParams = new RelativeLayout.
                                LayoutParams(
                                RelativeLayout.LayoutParams.MATCH_PARENT,
                                RelativeLayout.LayoutParams.MATCH_PARENT);
                        bestBadgeParams.addRule(RelativeLayout.RIGHT_OF, imageId);
                        bestBadgeParams.addRule(RelativeLayout.BELOW, specificationViewId);
                        bestBadgeParams.topMargin = margin;
                        bestBadgeParams.bottomMargin = margin;
                        bestBadge.setLayoutParams(bestBadgeParams);
                    }

                    //Add the layout to the row
                    row.addView(rowLayout);

                    // Add row to TableLayout
                    tl.addView(row, new TableLayout.LayoutParams(TableLayout.LayoutParams.
                            FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
                }
                specNum++;
            }

        }
    }

    public void showRecommendation(Comparison comparison)
    //Shows which tablet(s) to select based on the overall scoring
    {
        //Get the ranking from the comparison
        List<Map.Entry<Tablet, Integer>> tabletRanking = comparison.getTabletRanking();

        //Find out if there is a clear winner in this ranking
        int firstTabletScore = tabletRanking.get(0).getValue();
        int secondTabletScore = tabletRanking.get(1).getValue();

        if (firstTabletScore > secondTabletScore)
        //Only show the first tablet, which is the winner
        {
            //Get the tablet information
            final Tablet tablet = tabletRanking.get(0).getKey();

            //List the tablet in the table
            TableLayout tl = (TableLayout) getActivity().findViewById(R.id.comparisonTable);
            int margin = (int) getResources().getDimension(R.dimen.normal_margin);
            int tableMargin = (int) getResources().getDimension(R.dimen.table_margin);

            TableRow row = new TableRow(this.getActivity());
            TableLayout.LayoutParams tableRowParams =
                    new TableLayout.LayoutParams
                            (TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.
                                    MATCH_PARENT);
            tableRowParams.topMargin = tableMargin;
            tableRowParams.bottomMargin = tableMargin;
            tableRowParams.width = 0;
            tableRowParams.weight = 1;
            row.setLayoutParams(tableRowParams);

            //Create a new relative layout to hold views in the row
            RelativeLayout rowLayout = new RelativeLayout(this.getActivity());

            //Create a new image within the row layout
            ImageView image = new ImageView(this.getActivity());
            int imageId = Helper.generateViewId();
            image.setId(imageId);
            rowLayout.addView(image);

            //Set the image and set its layout
            image.setImageResource(tablet.getImage());
            image.setScaleType(ImageView.ScaleType.FIT_XY);
            RelativeLayout.LayoutParams imageLayoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);
            imageLayoutParams.width = (int) getResources().getDimension(R.dimen.image_width);
            imageLayoutParams.height = (int) getResources().getDimension(R.dimen.image_height);
            imageLayoutParams.setMargins(0, 0, margin, 0);
            imageLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            image.setLayoutParams(imageLayoutParams);

            //Add the tablet name
            TextView tabletName = new TextView(this.getActivity());
            int tabletNameId = Helper.generateViewId();
            tabletName.setId(tabletNameId);
            rowLayout.addView(tabletName);

            //Set the text its layout
            tabletName.setText(tablet.getName());
            tabletName.setTextAppearance(this.getActivity(), android.R.style.
                    TextAppearance_DeviceDefault_Medium);
            RelativeLayout.LayoutParams tabletNameParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);
            tabletNameParams.addRule(RelativeLayout.RIGHT_OF, imageId);
            tabletName.setLayoutParams(tabletNameParams);

            //Create a summary textview within the row layout
            TextView summaryView = new TextView(this.getActivity());
            int summaryViewId = Helper.generateViewId();
            summaryView.setId(summaryViewId);
            rowLayout.addView(summaryView);

            //Set the summary text its layout
            summaryView.setText(getResources().getString(R.string.winning_tablet_text));
            summaryView.setTextAppearance(this.getActivity(), android.R.style.
                    TextAppearance_DeviceDefault_Small);
            summaryView.setTypeface(null, Typeface.BOLD);
            RelativeLayout.LayoutParams specificationViewParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);
            specificationViewParams.addRule(RelativeLayout.RIGHT_OF, imageId);
            specificationViewParams.addRule(RelativeLayout.BELOW, tabletNameId);
            summaryView.setLayoutParams(specificationViewParams);

            //Create a checkbox in the row layout
            CheckBox selection = new CheckBox(this.getActivity());
            int selectionId = Helper.generateViewId();
            selection.setId(selectionId);
            rowLayout.addView(selection);

            //Set the checkbox text and its layout
            selection.setText(getResources().getString(R.string.favorites_checkbox_text));
            selection.setTextAppearance(this.getActivity(),
                    android.R.style.TextAppearance_DeviceDefault_Small);
            RelativeLayout.LayoutParams cbParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);
            cbParams.addRule(RelativeLayout.RIGHT_OF, imageId);
            cbParams.addRule(RelativeLayout.BELOW, summaryViewId);
            cbParams.bottomMargin = margin;
            cbParams.topMargin = margin;
            selection.setLayoutParams(cbParams);

            //set the checkbox state based on intent data
            if (tablet.getFavorited())
            {
                selection.setChecked(true);
            }

            //Set a listener for the state of the checkbox
            selection.setOnClickListener(new View.OnClickListener()
            {

                @Override
                public void onClick(View v)
                {
                    //Find out which tablet this checkbox belongs to
                    CheckBox selection = (CheckBox) v;

                    if (selection.isChecked())
                    //Set the global variable to indicate that a tablet was checked
                    {
                        tablet.setFavorited(true);
                    }
                    else
                    //Set the global variable to indicate that a tablet was unchecked
                    {
                        tablet.setFavorited(false);
                    }

                }
            });


            //Add the layout to the row
            row.addView(rowLayout);

            // Add row to TableLayout
            tl.addView(row, new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT));

        } else
        //Show however many tablets share the highest score
        {

            for (Map.Entry<Tablet, Integer> tabletEntry : tabletRanking) {

                //Get the tablet information
                final Tablet tablet = tabletEntry.getKey();
                float tabletScore = tabletEntry.getValue();

                if (tabletScore == firstTabletScore)
                //Find out if the tablet score equals the highest score
                {

                    //List the tablet in the table
                    TableLayout tl = (TableLayout) getActivity().findViewById(R.id.comparisonTable);
                    int margin = (int) getResources().getDimension(R.dimen.normal_margin);
                    int tableMargin = (int) getResources().getDimension(R.dimen.table_margin);

                    TableRow row = new TableRow(this.getActivity());
                    TableLayout.LayoutParams tableRowParams =
                            new TableLayout.LayoutParams
                                    (TableLayout.LayoutParams.MATCH_PARENT,
                                            TableLayout.LayoutParams.MATCH_PARENT);
                    tableRowParams.topMargin = tableMargin;
                    tableRowParams.bottomMargin = tableMargin;
                    tableRowParams.width = 0;
                    tableRowParams.weight = 1;
                    row.setLayoutParams(tableRowParams);

                    //Create a new relative layout to hold views in the row
                    RelativeLayout rowLayout = new RelativeLayout(this.getActivity());

                    //Create a new image within the row layout
                    ImageView image = new ImageView(this.getActivity());
                    int imageId = Helper.generateViewId();
                    image.setId(imageId);
                    rowLayout.addView(image);

                    //Set the cover image and set its layout
                    image.setImageResource(tablet.getImage());
                    image.setScaleType(ImageView.ScaleType.FIT_XY);
                    RelativeLayout.LayoutParams imageLayoutParams = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT,
                            RelativeLayout.LayoutParams.MATCH_PARENT);
                    imageLayoutParams.width = (int) getResources().getDimension(R.dimen.image_width);
                    imageLayoutParams.height = (int) getResources().getDimension(R.dimen.image_height);
                    imageLayoutParams.setMargins(0, 0, margin, 0);
                    imageLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    image.setLayoutParams(imageLayoutParams);

                    //Add the tablet name
                    TextView tabletName = new TextView(this.getActivity());
                    int tabletNameId = Helper.generateViewId();
                    tabletName.setId(tabletNameId);
                    rowLayout.addView(tabletName);

                    //Set the text its layout
                    tabletName.setText(tablet.getName());
                    tabletName.setTextAppearance(this.getActivity(), android.R.style.
                            TextAppearance_DeviceDefault_Medium);
                    RelativeLayout.LayoutParams tabletNameParams = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT,
                            RelativeLayout.LayoutParams.MATCH_PARENT);
                    tabletNameParams.addRule(RelativeLayout.RIGHT_OF, imageId);
                    tabletName.setLayoutParams(tabletNameParams);

                    //Create a summary textview within the row layout
                    TextView summaryView = new TextView(this.getActivity());
                    int summaryViewId = Helper.generateViewId();
                    summaryView.setId(summaryViewId);
                    rowLayout.addView(summaryView);

                    //Set the summary text its layout
                    summaryView.setText(getResources().getString(R.string.equal_winning_tablet_text));
                    summaryView.setTextAppearance(this.getActivity(),
                            android.R.style.TextAppearance_DeviceDefault_Small);
                    summaryView.setTypeface(null, Typeface.BOLD);
                    RelativeLayout.LayoutParams specificationViewParams = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT,
                            RelativeLayout.LayoutParams.MATCH_PARENT);
                    specificationViewParams.addRule(RelativeLayout.RIGHT_OF, imageId);
                    specificationViewParams.addRule(RelativeLayout.BELOW, tabletNameId);
                    summaryView.setLayoutParams(specificationViewParams);

                    //Create a checkbox in the row layout
                    CheckBox selection = new CheckBox(this.getActivity());
                    int selectionId = Helper.generateViewId();
                    selection.setId(selectionId);
                    rowLayout.addView(selection);

                    //Set the checkbox text and its layout
                    selection.setText(getResources().getString(R.string.favorites_checkbox_text));
                    selection.setTextAppearance(this.getActivity(),
                            android.R.style.TextAppearance_DeviceDefault_Small);
                    RelativeLayout.LayoutParams cbParams = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT,
                            RelativeLayout.LayoutParams.MATCH_PARENT);
                    cbParams.addRule(RelativeLayout.RIGHT_OF, imageId);
                    cbParams.addRule(RelativeLayout.BELOW, summaryViewId);
                    cbParams.bottomMargin = margin;
                    cbParams.topMargin = margin;
                    selection.setLayoutParams(cbParams);

                    //set the checkbox state based on intent data
                    if (tablet.getFavorited())
                    {
                        selection.setChecked(true);
                    }


                    //Set a listener for the state of the checkbox
                    selection.setOnClickListener(new View.OnClickListener()
                    {

                        @Override
                        public void onClick(View v)
                        {
                            //Find out which tablet this checkbox belongs to
                            CheckBox selection = (CheckBox) v;

                            if (selection.isChecked())
                            //Set the global variable to indicate that a tablet was checked
                            {
                                tablet.setFavorited(true);
                            }
                            else
                            //Set the global variable to indicate that a tablet was unchecked
                            {
                                tablet.setFavorited(false);
                            }

                        }
                    });


                    //Add the layout to the row
                    row.addView(rowLayout);

                    // Add row to TableLayout
                    tl.addView(row, new TableLayout.LayoutParams(TableLayout.LayoutParams.
                            FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

                }

            }
        }
    }
}
