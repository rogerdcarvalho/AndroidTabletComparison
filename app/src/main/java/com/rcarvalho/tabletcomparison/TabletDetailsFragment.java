package com.rcarvalho.tabletcomparison;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
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


/**
This fragment shows the details of a given tablet. It allows the user to add the tablet to favorites
 */
public class TabletDetailsFragment extends Fragment {

    //The tablet to be shown
    Tablet tablet;

    public TabletDetailsFragment()
    //Constructor: Not required in this context
    {
    }

/******************************** Main Activity Methods *******************************************/

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    //When the activity is created, load up Intent data and show book details
    {
        //Call superclass method
        super.onActivityCreated(savedInstanceState);

        //Get view components
        Button closeButton = (Button) getActivity().findViewById(R.id.detail_close_button);

        //Set button to close the fragment
        closeButton.setOnClickListener
                //When someone taps the button, close the fragment and return intent data
                (
                        new View.OnClickListener()
                        {
                            public void onClick(View v)
                            {
                                closeFragment();
                            }
                        }
                );

        //Get intent data
        Intent intent = getActivity().getIntent();
        Bundle bundle = intent.getExtras();

        if (bundle != null)
        //Receive the tablet the user wants to see
        {
            Object tabletObject = getActivity().getIntent().getParcelableExtra("Tablet");
            tablet = (Tablet) tabletObject;
            tablet.setFavorited(getActivity().getIntent().getExtras().getBoolean("Favorite"));
        }

        //Setup the tablet title, description and image
        showBasics();

        //Show the tablet information
        showDetails();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tablet_details, container, false);
    }

/*************************************** Subroutines **********************************************/

    public void closeFragment()
    //Closes the fragment and returns to the previous fragment with Intent data. Can be called
    // from the parent activity
    {
        //Prepare intent data
        Intent data = new Intent();
        data.putExtra("Tablet", tablet);

        //Close activity and return intent data
        getActivity().setResult(Activity.RESULT_OK, data);
        getActivity().finish();
    }


    private void showBasics()
    //Shows the tablet image, description and a checkbox to add it to favorites
    {
        TableLayout tl = (TableLayout) getActivity().findViewById(R.id.detailTable);
        int margin = (int) getResources().getDimension(R.dimen.normal_margin);
        int tableMargin = (int) getResources().getDimension(R.dimen.table_margin);

        //Create a new row in the table that will hold the section title
        TableRow row = new TableRow(this.getActivity());
        TableLayout.LayoutParams tableRowParams =
                new TableLayout.LayoutParams
                        (TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT);
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
        imageLayoutParams.setMargins(0,0, margin, 0 );
        imageLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        image.setLayoutParams(imageLayoutParams);

        //Create a title textview within the row layout
        TextView title = new TextView(this.getActivity());
        int titleId = Helper.generateViewId();
        title.setId(titleId);
        rowLayout.addView(title);

        //Set the title text its layout
        title.setText(tablet.getName());
        title.setTextAppearance(this.getActivity(), android.R.style.TextAppearance_DeviceDefault_Medium);
        RelativeLayout.LayoutParams titleParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        titleParams.addRule(RelativeLayout.RIGHT_OF, imageId);
        title.setLayoutParams(titleParams);

        //Create a subtitle textview within the row layout
        TextView subtitle = new TextView(this.getActivity());
        int subtitleId = Helper.generateViewId();
        subtitle.setId(subtitleId);
        rowLayout.addView(subtitle);

        //Set the subtitle text its layout
        subtitle.setText(tablet.getDescription());
        subtitle.setTextAppearance(this.getActivity(), android.R.style.TextAppearance_DeviceDefault_Small);
        RelativeLayout.LayoutParams subtitleParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        subtitleParams.addRule(RelativeLayout.RIGHT_OF, imageId);
        subtitleParams.addRule(RelativeLayout.BELOW, titleId);
        subtitle.setLayoutParams(subtitleParams);

        //Create a checkbox in the row layout
        CheckBox selection = new CheckBox(this.getActivity());
        int selectionId = Helper.generateViewId();
        selection.setId(selectionId);
        rowLayout.addView(selection);

        //Set the checkbox text and its layout
        selection.setText(getResources().getString(R.string.favorites_checkbox_text));
        selection.setTextAppearance(this.getActivity(), android.R.style.TextAppearance_DeviceDefault_Small);
        RelativeLayout.LayoutParams cbParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        cbParams.addRule(RelativeLayout.RIGHT_OF, imageId);
        cbParams.addRule(RelativeLayout.BELOW, subtitleId);
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
    }

    public void showDetails()
    //Lists all the specs of the Tablet
    {
        //Get the specifications that need to be displayed
        Specification[] specifications = tablet.getSpecs();
        int numSpecifications = specifications.length;

        for (int i = 0; i < numSpecifications; i++)
        //Loop through each available spec
        {
            Specification spec = specifications[i];

            //Find out if the specification should be displayed
            if (spec.getScorable())
            {
                //Find Tablelayout defined in main.xml
                TableLayout tl = (TableLayout) getActivity().findViewById(R.id.detailTable);
                int margin = (int) getResources().getDimension(R.dimen.normal_margin);
                int tableMargin = (int) getResources().getDimension(R.dimen.table_margin);

                //Create a new row in the table that will hold the section title
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

                //Align the title to the right
                title.setGravity(Gravity.CENTER);
                row.setGravity(Gravity.CENTER);

                //Add a background color
                row.setBackgroundColor(getResources().getColor(R.color.button_material_light));

                // Add row to TableLayout
                tl.addView(row, new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT,
                        TableLayout.LayoutParams.WRAP_CONTENT));

                //Now add the tablet data
                row = new TableRow(this.getActivity());
                tableRowParams =
                        new TableLayout.LayoutParams
                                (TableLayout.LayoutParams.MATCH_PARENT,
                                        TableLayout.LayoutParams.MATCH_PARENT);
                tableRowParams.topMargin = tableMargin;
                tableRowParams.bottomMargin = tableMargin;
                tableRowParams.width = 0;
                tableRowParams.weight = 1;
                row.setLayoutParams(tableRowParams);

                //Create a new relative layout to hold views in the row
                rowLayout = new RelativeLayout(this.getActivity());

                //Create a specification textview within the row layout
                TextView specificationView = new TextView(this.getActivity());
                int specificationViewId = Helper.generateViewId();
                specificationView.setId(specificationViewId);
                rowLayout.addView(specificationView);

                //Set the specification text its layout
                String specText = spec.toString();
                specificationView.setText(specText);
                specificationView.setTextAppearance(this.getActivity(),
                        android.R.style.TextAppearance_DeviceDefault_Small);
                RelativeLayout.LayoutParams specificationViewParams = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT);
                specificationView.setLayoutParams(specificationViewParams);

                //Add the layout to the row
                row.addView(rowLayout);

                // Add row to TableLayout
                tl.addView(row, new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT,
                        TableLayout.LayoutParams.WRAP_CONTENT));


            }
        }
    }
}
