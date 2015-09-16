package com.rcarvalho.tabletcomparison;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;


/**
This is the main fragment of this app. It shows all available tablets and allows the user
 to select which ones they would like to compare
 */
public class TabletSelectionFragment extends Fragment
{
    //Constants to indicate how many tablets can be compared
    public final int MAX_NUM_COMPARABLE = 6;
    public final int MIN_NUM_COMPARABLE = 2;

    //Tablets: This stores all available tablets
    private HashMap<Integer, Tablet> tablets = new HashMap<>();

    //Selections: This array holds which of the available tablets were selected for comparison
    private ArrayList<Boolean> selections = new ArrayList<>();
    private ArrayList<CheckBox> selectionViews = new ArrayList<>();

    //Tablet favorite indicators: Keeps track of the views that indicate whether a tablet was
    //favorited or not
    private HashMap<Integer, TextView> tabletFavoriteIndicators = new HashMap<>();

    //Scrollview: Allows us to scroll up when we want to 'Refresh' the view
    ScrollView scroller;

    //Number of selected Tablets: A counter that keeps track of how many tablets were selected
    private int numSelected = 0;

    //Intent references: Used to keep track of which fragment was just viewed
    static final int REQUEST_TABLET_DETAILS = 1; /* Used for the TabletDetails Fragment */
    static final int REQUEST_TABLET_COMPARISON = 2; /* Used for the TabletComparison Fragment */

    //SharedPreference reference: Used to store data across app sessions
    public static final String SHARED_PREFERENCES = "preferences";

    //Compare Button: The button on top of the screen
    Button compareButton;

/******************************** Main Activity Methods *******************************************/

    public TabletSelectionFragment()
    //Constructor. Not required in this context.
    {

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    /*
    As soon as Android loads the activity, import tablets from the "Assets" folder and load views
    */
    {
        //Call the super
        super.onActivityCreated(savedInstanceState);

        //Setup a XML parser
        XmlPullParserFactory pullParserFactory;

        try
        {
            //Setup the parser and input file
            pullParserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = pullParserFactory.newPullParser();
            InputStream in_s = this.getActivity().getApplicationContext().getAssets().
                    open("tablets.xml");
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in_s, null);

            //Import the tablets
            parseXML(parser);

        }
        catch (XmlPullParserException e)
        {
            //If the parser cannot be loaded, inform the user
            showDialog(getResources().getString(R.string.error_msgbox_title),
                    getResources().getString(R.string.xml_error), true);
            e.printStackTrace();
        }
        catch (IOException e)
        {
            //If the input file cannot be loaded, inform the user
            showDialog(getResources().getString(R.string.error_msgbox_title),
                    getResources().getString(R.string.xml_error), true);
            e.printStackTrace();
        }

        //Get the scrollView reference
        scroller = (ScrollView) getActivity().findViewById(R.id.scrollView);

        //Get the button reference
        compareButton = (Button) getActivity().findViewById(R.id.compare_button);

        //Set the button greyed out by default
        compareButton.setEnabled(false);

        compareButton.setOnClickListener
        //When someone taps the button, run a comparison
        (
            new View.OnClickListener()
                        {
                            public void onClick(View v)
                            {
                                prepareComparison();
                            }
                        }
        );

        //Draw all tablet information in the table
        showTablets();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    //Default onCreateView Method
    {
        return inflater.inflate(R.layout.fragment_tablet_selection, container, false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    //After a user has closed another view, update favorite indicators
    {
        //Declare variables
        String id;
        boolean tabletFavorited = false;

        if (requestCode == REQUEST_TABLET_COMPARISON)
        //The user has just returned from the TabletComparison Fragment
        {
            // Make sure the request was successful
            if (resultCode == Activity.RESULT_OK)
            {
                if (data.hasExtra("Tablets"))
                {
                    //Receive the array of tablets that were just compared (as Parcelable object)
                    Parcelable[]comparedTablets = data.getParcelableArrayExtra("Tablets");
                    ArrayList<Tablet> receivedTablets = new ArrayList<>();

                    //Convert the Parcelable object array to Tablet objects
                    for (Parcelable object : comparedTablets)
                    {
                        //Get the main tablet object (this holds whether it was favorited or not)
                        Tablet tablet = (Tablet)object;

                        //Look up the favorite indicator views for each tablet
                        TextView favoriteIndicator = tabletFavoriteIndicators.get(tablet.getId());

                        //Update the shared preferences that will save the data across user sessions
                        SharedPreferences.Editor editor = getActivity().getSharedPreferences
                                (SHARED_PREFERENCES, getActivity().MODE_PRIVATE).edit();
                        editor.putBoolean(Integer.toString(tablet.getId()), tablet.getFavorited());
                        editor.commit();

                        //Update the favorite indicators and public tablets map with any changes
                        tablets.put(tablet.getId(), tablet);

                        if (tablet.getFavorited())
                        {
                            favoriteIndicator.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            favoriteIndicator.setVisibility(View.INVISIBLE);
                        }
                    }
                }
                if (data.hasExtra("Reset"))
                {
                    boolean resetSelections = data.getBooleanExtra("Reset", false);

                    if (resetSelections)
                    {
                        clearSelections();
                        scroller.scrollTo(0,0);
                    }
                }
            }
        }
        else if (requestCode == REQUEST_TABLET_DETAILS)
        //The user has just returned from the Tablet Details View
        {
            // Make sure the request was successful
            if (resultCode == Activity.RESULT_OK)
            {
                if (data.hasExtra("Tablet"))
                {
                    //Receive the tablets that was just viewed
                    Tablet activeTablet = data.getParcelableExtra("Tablet");

                    //Find out if it was favorited and locate its matching favorite view
                    tabletFavorited = activeTablet.getFavorited();
                    id = Integer.toString(activeTablet.getId());
                    TextView favoriteIndicator = tabletFavoriteIndicators.get(activeTablet.getId());

                    //Update the shared preferences that will save the data across user sessions
                    SharedPreferences.Editor editor = getActivity().getSharedPreferences
                            (SHARED_PREFERENCES, getActivity().MODE_PRIVATE).edit();
                    editor.putBoolean(id, tabletFavorited);
                    editor.commit();

                    //Update the tablets map with any changes and update views
                    tablets.put(activeTablet.getId(), activeTablet);

                    if (tabletFavorited) {
                        favoriteIndicator.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        favoriteIndicator.setVisibility(View.INVISIBLE);
                    }
                }
                else
                {
                    Log.i("Testing", "onActivityResult result is not RESULT_NOT_OK");
                }
            }
        }
    }

/*************************************** Subroutines **********************************************/

    private void clearSelections()
    //This methods unchecks all tablets for comparison
    {
        int i = 0;
        for (CheckBox selection : selectionViews)
        {
            selection.setChecked(false);
            selections.set(i, false);
            i++;
        }
        numSelected = 0;
        compareButton.setEnabled(false);
    }

    private void loadTabletDetails(Tablet tablet)
    //This method loads up the Tablet Details fragment with a given tablet
    {
        //Prepare the intent
        Intent intent = new Intent(getActivity(), TabletDetails.class);

        //Determine whether this tablet was favorited or not (from the Shared Preferences)
        SharedPreferences prefs = getActivity().getSharedPreferences(SHARED_PREFERENCES,
                getActivity().MODE_PRIVATE);
        boolean favorited =  prefs.getBoolean(Integer.toString(tablet.getId()), false);


        intent.putExtra("Tablet", tablet);
        intent.putExtra("Favorite", favorited);

        startActivityForResult(intent, REQUEST_TABLET_DETAILS);

    }

    private void openComparison(Tablet[] tablets)
    //This method opens the TabletComparison Fragement with an array of Tablets
    {
        //Prepare the intent
        Intent intent = new Intent(getActivity(), TabletComparison.class);
        intent.putExtra("Tablets", tablets);

        //Open the fragement and request result data
        startActivityForResult(intent, REQUEST_TABLET_COMPARISON);
    }


    private void parseXML(XmlPullParser parser) throws XmlPullParserException,IOException
    //This method loads tablet data from the XML stored under "Assets"
    {
        int eventType = parser.getEventType();
        Tablet currentTablet = null;
        String name;

        //As we will derive screen resolution and dimensions after processing, store the required
        //data in public variables
        String tabletLength = null;
        String tabletWidth = null;
        String tabletResHeight = null;
        String tabletResWidth = null;

        float tabletLengthValue = 0;
        float tabletWidthValue = 0;
        float tabletResHeightValue = 0;
        float tabletResWidthValue= 0;

        while (eventType != XmlPullParser.END_DOCUMENT)
        {
            int xmlpp = XmlPullParser.START_TAG;

            switch (eventType)
            {
                case XmlPullParser.START_TAG:

                    //A new XML Tag was read. Find out what is in it
                    name = parser.getName();

                    try
                    {
                        switch (name)
                        {
                            case "product":
                            //A new tablet was read. Prepare the tablet object
                                currentTablet = new Tablet();
                                break;
                            case "productid":
                                String tabletId = parser.nextText();
                                currentTablet.setId(Integer.parseInt(tabletId));
                                break;
                            case "productname":
                                String tabletName = parser.nextText();
                                currentTablet.setName(tabletName);
                                break;
                            case "productcores":
                                String tabletCores = parser.nextText();
                                float tabletCoresValue = Float.parseFloat(tabletCores);
                                Specification cores = new Specification(getResources().getString
                                        (R.string.cores_specification_name),
                                        tabletCoresValue,tabletCores,0,getResources().getString
                                        (R.string.cores_specification_units),true,true,true,false);
                                currentTablet.setSpec(cores);
                                break;
                            case "productclockspeed":
                                String tabletClockSpeed = parser.nextText();
                                float tabletClockSpeedValue = Float.parseFloat(tabletClockSpeed);
                                Specification clockspeed = new Specification(getResources().
                                        getString(R.string.clockspeed_specification_name),
                                        tabletClockSpeedValue,tabletClockSpeed,1,getResources().
                                        getString(R.string.clockspeed_specification_units),true,true,
                                        true,false);
                                currentTablet.setSpec(clockspeed);
                                break;
                            case "productmemory":
                                String tabletMemory = parser.nextText();
                                float tabletMemoryValue =Float.parseFloat(tabletMemory);
                                Specification memory = new Specification(getResources().
                                        getString(R.string.memory_specification_name),
                                        tabletMemoryValue,tabletMemory,0,getResources().
                                        getString(R.string.memory_specification_units),true,true,
                                        true,false);
                                currentTablet.setSpec(memory);
                                break;
                            case "productstorage":
                                String tabletStorage = parser.nextText();
                                float tabletStorageValue = Float.parseFloat(tabletStorage);
                                Specification storage = new Specification(getResources().getString
                                        (R.string.storage_specification_name),
                                    tabletStorageValue,tabletStorage,0,getResources().getString
                                        (R.string.storage_specification_units),true,true,
                                    true,false);
                                currentTablet.setSpec(storage);
                                break;
                            case "productmsrp":
                                String tabletPrice = parser.nextText();
                                float tabletPriceValue = Float.parseFloat(tabletPrice);
                                Specification price = new Specification(getResources().getString
                                        (R.string.price_specification_name), tabletPriceValue,
                                        tabletPrice, 2, getResources().getString(R.string.
                                        price_specification_units),false,false,true,false);
                                currentTablet.setSpec(price);
                                break;
                            case "productscreen":
                                String tabletScreen = parser.nextText();
                                float tabletScreenValue = Float.parseFloat(tabletScreen);
                                Specification screen = new Specification(getResources().
                                        getString(R.string.screen_specification_name),
                                        tabletScreenValue,tabletScreen,1,getResources().
                                        getString(R.string.screen_specification_units),true,true,
                                        true,false);
                                currentTablet.setSpec(screen);
                                break;
                            case "productweight":
                                String tabletWeight = parser.nextText();
                                float tabletWeightValue = Float.parseFloat(tabletWeight);
                                Specification weight = new Specification(getResources().getString
                                        (R.string.weight_specification_name),tabletWeightValue,
                                        tabletWeight, 1,getResources().getString(R.string.
                                        weight_specification_units), true, false, true, false);
                                currentTablet.setSpec(weight);
                                break;
                            case "productheight":
                                String tabletHeight = parser.nextText();
                                float tabletHeightValue = Float.parseFloat(tabletHeight);
                                Specification height = new Specification(getResources().
                                        getString(R.string.height_specification_name),tabletHeightValue,
                                        tabletHeight, 1,getResources().getString
                                        (R.string.height_specification_units), true, false, true, false);
                                currentTablet.setSpec(height);
                                break;
                            case "productlength":
                                tabletLength = parser.nextText();
                                tabletLengthValue = Float.parseFloat(tabletLength);
                                Specification length = new Specification(getResources().
                                        getString(R.string.length_specification_name),tabletLengthValue,
                                        tabletLength,0,getResources().getString
                                        (R.string.length_specification_units),true,true,false,false);
                                currentTablet.setSpec(length);
                                break;
                            case "productwidth":
                                tabletWidth = parser.nextText();
                                tabletWidthValue = Float.parseFloat(tabletWidth);
                                Specification width = new Specification(getResources().
                                        getString(R.string.width_specification_name),tabletWidthValue,
                                        tabletWidth,0,getResources().
                                        getString(R.string.width_specification_units),true,true,false,false);
                                currentTablet.setSpec(width);
                                break;
                            case "productreswidth":
                                tabletResWidth = parser.nextText();
                                tabletResWidthValue = Float.parseFloat(tabletResWidth);
                                Specification resWidth = new Specification(getResources().
                                        getString(R.string.reswidth_specification_name),
                                        tabletResWidthValue, tabletResWidth,0,getResources().
                                        getString(R.string.reswidth_specification_units),true,true,
                                        false,false);
                                currentTablet.setSpec(resWidth);
                                break;
                            case "productresheight":
                                tabletResHeight = parser.nextText();
                                tabletResHeightValue = Float.parseFloat(tabletResHeight);
                                Specification resHeight = new Specification(getResources().
                                        getString(R.string.resheight_specification_name),
                                        tabletResHeightValue, tabletResHeight,0,getResources().
                                        getString(R.string.resheight_specification_units),true,true,
                                        false,false);
                                currentTablet.setSpec(resHeight);
                                break;
                            case "productos":
                                String tabletOs = parser.nextText();

                                //Get rid of any dots in the provided os
                                String noDotsOs = (tabletOs).replace(".", "");
                                int strLength = noDotsOs.length();

                                //Create a new string that has just one single dot
                                String osValueString = noDotsOs.substring(0, 1);
                                osValueString = osValueString + "." +
                                        noDotsOs.substring(1, strLength);

                                //Store this in the float
                                float osValue = Float.parseFloat(osValueString);
                                Specification os = new Specification(getResources().
                                        getString(R.string.os_specification_name),
                                        osValue, tabletOs,0,getResources().
                                        getString(R.string.os_specification_units),false,true,
                                        true,true);
                                currentTablet.setSpec(os);
                                break;
                            case "productimage":
                                String tabletImage = parser.nextText();
                                int resID = getResources().getIdentifier(tabletImage ,
                                        "drawable", getActivity().getPackageName());
                                currentTablet.setImage(resID);
                                break;
                            case "productdescription":
                                String tabletDescription = parser.nextText();
                                currentTablet.setDescription(tabletDescription);
                                break;
                        }

                    }

                    catch (NumberFormatException e)
                    {
                        //If any of the XML values could not be loaded, inform the user
                        showDialog(getResources().getString(R.string.error_msgbox_title),
                            getResources().getString(R.string.xml_error), true);
                        e.printStackTrace();
                    }

                case XmlPullParser.END_TAG:
                    name = parser.getName();
                    if (name.equalsIgnoreCase("product") && currentTablet.getId() != 0)
                    {
                        //Now that the tablet has all its core specifications, add 2 derived specs
                        float fullResolution = tabletResHeightValue * tabletResWidthValue;
                        String resolutionText = tabletResWidth + "x" + tabletResHeight;
                        Specification resolution = new Specification(getResources().getString
                                (R.string.resolution_specification_name),
                                fullResolution, resolutionText,0,getResources().getString
                                (R.string.resolution_specification_units),true,true,true,true);
                        currentTablet.setSpec(resolution);

                        float fullDimensions = tabletLengthValue * tabletWidthValue;
                        String dimensionsText = tabletWidth + getResources().getString
                                (R.string.dimensions_specification_units_w) + " x " + tabletLength +
                                getResources().getString
                                (R.string.dimensions_specification_units_l);
                        Specification dimensions = new Specification(getResources().getString
                                (R.string.dimensions_specification_name),fullDimensions,
                                dimensionsText,0,"",true,true,true,true);
                        currentTablet.setSpec(dimensions);

                        tablets.put(currentTablet.getId(), currentTablet);
                        selections.add(false);
                    }
            }
            eventType = parser.next();
        }
    }

    private void prepareComparison()
    //This method prepares a comparison. It checks how many tablets were selected and kicks off
    //the comparison process if allowed
    {
        if (numSelected < MIN_NUM_COMPARABLE)
        //The user did not select enough tablets to run a comparison. Inform them.
        {
            Toast.makeText(getActivity().getApplicationContext(), getResources().
                            getString(R.string.not_enough_tablets_selected),
                    Toast.LENGTH_LONG).show();
        }
        else if (numSelected > MAX_NUM_COMPARABLE)
        //The user selected too many tablets to run a comparison on.
        {
            Toast.makeText(getActivity().getApplicationContext(), getResources().
                            getString(R.string.too_many_tablets_selected),
                    Toast.LENGTH_LONG).show();
        }
        else
        //Run the comparison
        {
            //Prepare a clean array that the comparison can be run on
            ArrayList<Tablet> selected = new ArrayList<>();

            //Loop through all tabets
            int i = 0;
            for (Tablet tablet : tablets.values() )
            {
                if (selections.get(i))
                //If the tablet was selected, add it to the clean array
                {
                    selected.add(tablet);
                }
                i++;
            }

            //Convert the clean arraylist to a normal array and start comparison
            Tablet[] selectedArray = new Tablet[selected.size()];
            selectedArray = selected.toArray(selectedArray);
            openComparison(selectedArray);
        }
    }

    private void showDialog(String title, String message, boolean terminateApp)
    /*
    This method spawns a dialog box with a Close button. It can either terminate the app, or simply
    dismiss when pressing this button
    */
    {
        //Setup dialog
        AlertDialog ad = new AlertDialog.Builder(this.getActivity()).create();
        ad.setCancelable(false);
        ad.setTitle(title);
        ad.setMessage(message);

        if (terminateApp)
        //Close the app upon pressing the Close button
        {
            ad.setButton(getResources().getString(R.string.msgbox_close_button_title),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    });
        }
        else
        //Dismiss the dialogue upon pressing the Close button
        {
            ad.setButton(getResources().getString(R.string.msgbox_close_button_title),
                    new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.dismiss();
                        }
                    });
        }
        ad.show();
    }


    public void showTablets()
    //This method loads all available tablets in the table layout of this fragment
    {
        //Variable to keep track of the tablet index
        int tabletNum = 0;

        for (final Tablet tablet : tablets.values())
        //Loop through each available tablet
        {

            int id = tablet.getId();

            //Find Tablelayout defined in main.xml
            TableLayout tl = (TableLayout) getActivity().findViewById(R.id.tabletTable);
            int margin = (int) getResources().getDimension(R.dimen.normal_margin);
            int tableMargin = (int) getResources().getDimension(R.dimen.table_margin);

            //Create a new row in the table
            TableRow row = new TableRow(this.getActivity());
            TableLayout.LayoutParams tableRowParams=
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
            title.setTextAppearance(this.getActivity(), android.R.style.
                    TextAppearance_DeviceDefault_Medium);
            RelativeLayout.LayoutParams titleParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            titleParams.addRule(RelativeLayout.RIGHT_OF, imageId);
            title.setLayoutParams(titleParams);


            //Create a subtitle textview within the row layout
            TextView subtitle = new TextView(this.getActivity());
            int subtitleId = Helper.generateViewId();
            subtitle.setId(subtitleId);
            rowLayout.addView(subtitle);

            //Set the subtitle text its layout
            subtitle.setText(tablet.getDescription());
            subtitle.setTextAppearance(this.getActivity(), android.R.style.
                    TextAppearance_DeviceDefault_Small);
            RelativeLayout.LayoutParams subtitleParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);
            subtitleParams.addRule(RelativeLayout.RIGHT_OF, imageId);
            subtitleParams.addRule(RelativeLayout.BELOW, titleId);
            subtitle.setLayoutParams(subtitleParams);

            //Create a price textview within the row layout
            TextView price = new TextView(this.getActivity());
            int priceId = Helper.generateViewId();
            price.setId(priceId);
            rowLayout.addView(price);

            //Set the price text and its layout
            Specification[] tabletSpecs = tablet.getSpecs();
            float priceValue = tabletSpecs[4].getValue();
            String priceString = String.format(getResources().getString
                    (R.string.price_specification_units) + "%.2f", priceValue);
            price.setText(priceString);
            price.setTextAppearance(this.getActivity(),
                    android.R.style.TextAppearance_DeviceDefault_Small);
            price.setTypeface(null, Typeface.BOLD);
            RelativeLayout.LayoutParams priceParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);
            priceParams.addRule(RelativeLayout.RIGHT_OF, imageId);
            priceParams.addRule(RelativeLayout.BELOW, subtitleId);
            price.setLayoutParams(priceParams);

            //Create a checkbox in the row layout
            CheckBox selection = new CheckBox(this.getActivity());
            int selectionId = Helper.generateViewId();
            selection.setId(selectionId);
            rowLayout.addView(selection);

            //Keep track of which tablet this checkbox belongs to
            selection.setTag(tabletNum);

            //Set the checkbox text and its layout
            selection.setText(getResources().getString(R.string.selection_text));
            selection.setTextAppearance(this.getActivity(),
                    android.R.style.TextAppearance_DeviceDefault_Small);
            RelativeLayout.LayoutParams cbParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);
            cbParams.addRule(RelativeLayout.RIGHT_OF, imageId);
            cbParams.addRule(RelativeLayout.BELOW, priceId);
            cbParams.bottomMargin = margin;
            cbParams.topMargin = margin;
            selection.setLayoutParams(cbParams);

            //Add the view to the Fragment ArrayList
            selectionViews.add(selection);

            //Create the favorite indicator textview
            final TextView liked = new TextView(this.getActivity());
            int likedId = Helper.generateViewId();
            liked.setId(likedId);
            rowLayout.addView(liked);

            //Set the favorite indicator text and layout
            liked.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_important,0,0,0);

            RelativeLayout.LayoutParams likedParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);
            likedParams.addRule(RelativeLayout.RIGHT_OF, titleId);
            liked.setLayoutParams(likedParams);

            //Determine whether this book was favorited or not (from the Shared Preferences)
            SharedPreferences prefs = getActivity().getSharedPreferences(SHARED_PREFERENCES,
                    getActivity().MODE_PRIVATE);
            final boolean favoritedTablet = prefs.getBoolean(Integer.toString(id), false);
            tablet.setFavorited(favoritedTablet);

            tabletFavoriteIndicators.put(tablet.getId(), liked);

            //If the book wasn't favorited, hide the view
            if (!favoritedTablet)
            {
                liked.setVisibility(View.INVISIBLE);
            }

            //Set a listener for the image
            image.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    loadTabletDetails(tablets.get(tablet.getId()));
                } });

            //Set a listener for the state of the checkbox
            selection.setOnClickListener(new View.OnClickListener()
            {

                @Override
                public void onClick(View v)
                {
                    //Find out which tablet this checkbox belongs to
                    CheckBox selection = (CheckBox) v;
                    int selectionNum = (int)selection.getTag();

                    if (selection.isChecked())
                    //Set the global variable to indicate that a tablet was checked
                    {
                        selections.set(selectionNum, true);
                        numSelected++;
                        if (numSelected >= MIN_NUM_COMPARABLE)
                        //Enable the compare button
                        {
                            compareButton.setEnabled(true);

                        }
                        if (numSelected > MAX_NUM_COMPARABLE)
                        //Undo this selection
                        {
                            Toast.makeText(getActivity().getApplicationContext(), getResources().
                                            getString(R.string.too_many_tablets_selected),
                                    Toast.LENGTH_LONG).show();
                            selection.setChecked(false);
                            selections.set(selectionNum, false);
                            numSelected--;
                        }
                    }
                    else
                    //Set the global variable to indicate that a tablet was unchecked
                    {
                        selections.set(selectionNum, false);
                        numSelected--;
                        if (numSelected < 2)
                        {
                            compareButton.setEnabled(false);
                        }

                    }

                }
            });

            //Add the layout to the row
            row.addView(rowLayout);

            // Add row to TableLayout
            tl.addView(row, new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT));
            tabletNum++;
        }
    }
}
