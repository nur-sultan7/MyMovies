package com.example.mymovies.converters;

import androidx.room.TypeConverter;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class Converter {

    @TypeConverter
    public String getListAsString(List<Integer> genreIds)
    {
        return new Gson().toJson(genreIds);
    }
    @TypeConverter
    public List<Integer> getStringAsList(String genreIds)
    {
        Gson gson = new Gson();
        ArrayList arrayList = gson.fromJson(genreIds,ArrayList.class);
        ArrayList<Integer> list = new ArrayList<>();
        for (Object o: arrayList)
        {
            double d = Double.parseDouble(o.toString());
            list.add((int) d);
        }
        return list;
    }
}
