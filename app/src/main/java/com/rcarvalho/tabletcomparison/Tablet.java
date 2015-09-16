package com.rcarvalho.tabletcomparison;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
This class manages tablet objects, containing the details of a given tablet device
 It implements the Parcelable protocol to be transmissible via intents
 */
public class Tablet implements Parcelable
{
    //Specifications
    private ArrayList<Specification> specifications = new ArrayList<>();

    //Other Data
    private int id;
    private String name;
    private int image;
    private String description;
    private boolean favorited;

/*************************************** Constructors *********************************************/

    public Tablet()
    //null Constructor
    {

    }

/****************************************** Getters ***********************************************/

    public String getDescription()
    //Returns the description of this tablet
    {
        return description;
    }

    public boolean getFavorited()
    //Returns whether this tablet was favorited by the user
    {
        return favorited;
    }

    public int getId()
    //Returns the id of this tablet
    {
        return id;
    }

    public int getImage()
    //Returns the resource ID of the image of this tablet
    {
        return image;
    }

    public String getName()
    //Returns the name of this tablet
    {
        return name;
    }

    public Specification[] getSpecs()
    //Returns all the specifications of this tablet
    {
        Specification[] fullSpecs = new Specification[specifications.size()];
        return specifications.toArray(fullSpecs);
    }

/****************************************** Setters ***********************************************/

    public void setDescription(String description)
    {
        this.description = description;
    }

    public void setFavorited(boolean favorited)
    {
        this.favorited = favorited;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setId(int id)
    {
        this.id = id;
    }


    public void setImage(int image)
    {
        this.image = image;
    }

    public void setSpec(Specification spec)
    {
        specifications.add(spec);
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
        dest.writeList(specifications);
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeInt(image);
        dest.writeString(description);
        dest.writeByte((byte) (favorited ? 1 : 0));
    }

    private Tablet (Parcel in)
    //Method describing how to 'unpack' a parcel to a proper object
    {
        this.specifications = in.readArrayList(Tablet.class.getClassLoader());
        this.id = in.readInt();
        this.name = in.readString();
        this.image = in.readInt();
        this.description = in.readString();
        this.favorited = in.readByte() != 0;

    }

    //Required spec to make this object parcelable
    public static final Parcelable.Creator<Tablet> CREATOR = new Parcelable.Creator<Tablet>()
    {

        @Override
        public Tablet createFromParcel(Parcel source)
        {
            return new Tablet(source);
        }

        @Override
        public Tablet[] newArray(int size)
        {
            return new Tablet[size];
        }
    };
}
