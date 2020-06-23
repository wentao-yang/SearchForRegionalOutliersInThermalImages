# Outlier Flagger in Thermal Images [![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)

A Java program with GUI that regionalizes thermal images, checks for any cases where an area is surrounded by a much larger area with a noticeable temperature difference, and returns any that tests positive given the directory of a folder of grayscaled thermal images. 

## Background

During an UT Inventor Sprint, an UT employee voiced his issues regarding repairs of the rooftops of UT buildings. He flies a drone with a thermal camera over every rooftop and has to manually look at every single photo to determine whether repair was needed or not. He is looking for a way to lessen his workload. I noticed that every single rooftop that needed repair had a region where the temperature was much higher or lower than surrounding areas, so I created this application. This app regionalizes the rooftop, checks for any area where it is completely surrounded by a single much larger area with a noticeable temperature difference, and flags any that tests positive.

## Using the application

First, open up command prompt/terminal in this directory and enter this:

```
$ cd src
```

Next, compile the .java files. We only need to compile ```GUI.java``` since it contains the main method and calls ```ThermalImageCategorization.java```:

```
$ javac GUI.java
```

Then, we can run the application using:

```
$ java GUI
```

An interface will ask you to enter the directory of the folder with the thermal images. Once you submit, it will present the names of the image files that contains outliers.
