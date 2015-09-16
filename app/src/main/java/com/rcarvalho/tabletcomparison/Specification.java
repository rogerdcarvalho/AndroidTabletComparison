package com.rcarvalho.tabletcomparison;

import android.os.Parcel;
import android.os.Parcelable;

/**
This class handles technical specifications and the way they should be communicated to users.
 It implements the Parcelable protocol to be transmissible via intents
 */
public class Specification implements Parcelable
{
    //Variables used to manage specifications
    private String name;
    //The general name of the specification
    private float value;
    //The value of the specification
    private String valueText;
    //The value represented in text
    private int decimalPlaces;
    //The number of decimal places that are normally stored by this specification
    private String unit;
    //The unit description of the specification
    private boolean unitAfterValue;
    //Whether or not the unit is printed after the value
    private boolean rankingHighToLow;
    //Whether a higher value is better than a lower value
    private boolean scorable;
    //Whether this specification should be taken into consideration when comparing products
    private boolean stringOnly;
    //Whether this specification should only be represented in text and not in value

/*************************************** Constructors *********************************************/

    public Specification(String name, float value, String valueText, int decimalPlaces, String unit,
                         boolean unitAfterValue,boolean rankingHighToLow, boolean scorable,
                         boolean stringOnly)
    //Full constructor. Initializes a specification with all data required
    {
        this.name = name;
        this.value = value;
        this.valueText = valueText;
        this.decimalPlaces = decimalPlaces;
        this.unit = unit;
        this.unitAfterValue = unitAfterValue;
        this.rankingHighToLow = rankingHighToLow;
        this.scorable = scorable;
        this.stringOnly = stringOnly;

    }

/******************************************* Getters **********************************************/

    public String getName()
    //Returns the name of this specification
    {
        return name;
    }
    public boolean getRankingHighToLow()
    //Returns whether the specification should be ranked high to low or low to high
    {
        return rankingHighToLow;
    }

    public boolean getScorable()
    //Returns whether the specification should be taken into account in comparisons
    {
        return scorable;
    }

    public float getValue()
    //Returns the value of the specification
    {
        return value;
    }

    @Override
    public String toString()
    {
        if (stringOnly)
        {
            String space = " ";

            if (unitAfterValue)
            {
                return valueText + space + unit;
            }
            else
            {
                return unit + space + valueText;
            }
        }

        else
        {
            valueText = String.format("%." + Integer.toString(decimalPlaces) + "f", value);
            String space = " ";

            if (unitAfterValue)
            {
                return valueText + space + unit;
            }
            else
            {
                return unit + space + valueText;
            }
        }

    }
/*********************************** Parcelable Implementation ************************************/

    @Override
    public int describeContents()
    //Required method to make this object parcelable
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    //Method describing how to copy this object to a Parcel
    {
        dest.writeString(name);
        dest.writeFloat(value);
        dest.writeString(valueText);
        dest.writeInt(decimalPlaces);
        dest.writeString(unit);
        dest.writeByte((byte) (unitAfterValue ? 1 : 0));
        dest.writeByte((byte) (rankingHighToLow ? 1 : 0));
        dest.writeByte((byte) (scorable ? 1 : 0));
        dest.writeByte((byte) (stringOnly ? 1 : 0));

    }

    private Specification (Parcel in)
    //Method describing how to 'unpack' a parcel to a proper object
    {
        this.name = in.readString();
        this.value = in.readFloat();
        this.valueText = in.readString();
        this.decimalPlaces = in.readInt();
        this.unit = in.readString();
        this.unitAfterValue = in.readByte() != 0;
        this.rankingHighToLow = in.readByte() != 0;
        this.scorable = in.readByte() != 0;
        this.stringOnly = in.readByte() != 0;

    }

    //Required spec to make this object parcelable
    public static final Parcelable.Creator<Specification> CREATOR =
            new Parcelable.Creator<Specification>()
            {
                @Override
                public Specification createFromParcel(Parcel source)
                {
                    return new Specification(source);

                }

                @Override
                public Specification[] newArray(int size)
                {
                    return new Specification[size];
            }
        };

}