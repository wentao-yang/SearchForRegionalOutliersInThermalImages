import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import javax.imageio.ImageIO;

/**
 * Given a folder with .jpg's of gray-scaled thermal photos, the program reads
 * in the .jpg files pixel by pixel by its RGB values, categorizes regions of
 * similar temperature, and returns a list of files that contain a spot where
 * the temperature is significantly different than the temperature of spots
 * around it.
 * 
 * The project was created during a sprint to help U.T. Austin more easily
 * detect campus building roofs that potentially needed repairs rather than
 * manually checking every thermal image documented.
 * 
 * @author Wentao Yang
 * 
 */
public class ThermalImageCategorization {
	// Stores the average of the three RGB values per pixel per photo since the
	// photos are gray-scale thermal images
	private HashMap<String, int[][]> valRGB;
	// Stores the regions of a photo by color
	private HashMap<String, int[][]> regions;
	// Maximum difference between two neighboring pixel's average RGB values so that
	// they are grouped as one region. This depends on the settings of the thermal
	// camera, and changing this could change the result of the program and
	// increasing this requires significant amounts of stack memory
	private final int regionRange = 5;
	// Uses different methods instead of recursion
	private final boolean noRecur = true;

	/**
	 * Since the photos are gray-scaled, each of the three RGB values are all about
	 * the same, the program takes the average of those RGB values and store it in a
	 * 2D array for every .jpg to compare later on.
	 * 
	 * @param location is the directory of the folder with all the .jpg's
	 */
	public ThermalImageCategorization(String location) {
		if (location == null) {
			throw new IllegalArgumentException("Input cannot be null.");
		}

		try {
			valRGB = new HashMap<String, int[][]>();
			regions = new HashMap<String, int[][]>();

			// Gets every file in location
			File folder = new File(location);
			File[] listOfFiles = folder.listFiles();

			// Checks each file in directory
			for (File file : listOfFiles) {

				// Checks each file to see if it is a .jpg
				if (checkJPG(file)) {
					BufferedImage img = ImageIO.read(file);
					int[][] temp = new int[img.getWidth()][img.getHeight()];

					// Stores the average of the RGB values of each pixel into the 2D array
					for (int row = 0; row < temp.length; row++) {
						for (int col = 0; col < temp[0].length; col++) {
							// Gets each of the RGB values of the pixel
							int red = new Color(img.getRGB(row, col)).getRed();
							int green = new Color(img.getRGB(row, col)).getGreen();
							int blue = new Color(img.getRGB(row, col)).getBlue();
							temp[row][col] = (red + green + blue) / 3; // Since images are gray-scaled
						}
					}

					// Adds the 2D array into the HashMap photos
					valRGB.put(file.getName(), temp);
					createRegions(file.getName(), temp);
				}
			}
		} catch (Exception E) {
			System.out.println("Something went wrong in reading each file.");
		}
	}

	/**
	 * Creates a 2D array of the different regions of a given photo and stores it in
	 * the HashMap regions
	 * 
	 * @param name   is the name of the .jpg
	 * @param avgRGB is the 2D array of average RGB values of pixels whose regions
	 *               will be created for
	 */
	private void createRegions(String name, int[][] avgRGB) {
		try {
			if (noRecur) {
				regions.put(name, createRegionsHelperHash(avgRGB));
			} else {
				regions.put(name, createRegionsHelper(avgRGB));
			}
		} catch (Exception E) {
			System.out.println("Something went wrong in creating regions for each photo.");
		}
	}

	/**
	 * Create a 2D array of the different regions in a given photo using a Stack
	 * 
	 * @param avgRGB is the 2D array of average RGB values of pixels whose regions
	 *               will be created for
	 */
	private int[][] createRegionsHelperHash(int[][] avgRGB) {
		// Creates the variables used
		int[][] regionArray = new int[avgRGB.length][avgRGB[0].length];
		int numRegions = 1;

		// Runs for each point in the 2D array
		for (int r = 0; r < avgRGB.length; r++) {
			for (int c = 0; c < avgRGB[0].length; c++) {

				if (regionArray[r][c] == 0) { // We have not assigned this point to a region yet
					regionArray[r][c] = numRegions; // Create new region
					Stack<Point> temp = new Stack<Point>();
					temp.push(new Point(r, c));

					// While there are still points in this region
					while (!temp.isEmpty()) {
						Point currPoint = temp.pop();
						int row = currPoint.r;
						int col = currPoint.c;

						// Runs for the neighbors of the current point
						for (int num = -1; num <= 1; num += 2) {
							if (inBounds(avgRGB, row, col + num)) {
								// If the neighbor is part of this point's region
								if (regionArray[row][col + num] == 0
										&& Math.abs(avgRGB[row][col] - avgRGB[row][col + num]) <= regionRange) {
									regionArray[row][col + num] = numRegions;
									temp.push(new Point(row, col + num));
								}
							}
							if (inBounds(avgRGB, row + num, col)) {
								// If the neighbor is part of this point's region
								if (regionArray[row + num][col] == 0
										&& Math.abs(avgRGB[row][col] - avgRGB[row + num][col]) <= regionRange) {
									regionArray[row + num][col] = numRegions;
									temp.push(new Point(row + num, col));
								}
							}
						}
					}
					numRegions++;
				}
			}
		}
		return regionArray;
	}

	/**
	 * A class used to store the row and column number of a point
	 */
	private class Point {
		// Row and column
		int r, c;

		/**
		 * Constructor
		 * 
		 * @param row is the row number
		 * @param col is the column number
		 */
		public Point(int row, int col) {
			r = row;
			c = col;
		}
	}

	/**
	 * Create a 2D array of the different regions in a given photo recursively.
	 * 
	 * @param avgRGB is the 2D array of average RGB values of pixels whose regions
	 *               will be created for
	 */
	private int[][] createRegionsHelper(int[][] avgRGB) {
		// Creates the variables used
		int[][] regionArray = new int[avgRGB.length][avgRGB[0].length];
		int numRegions = 1; // Currently there is only 1 region
		createRegionsRecursion(avgRGB, regionArray, 0, 0, 0, 0, numRegions); // Checks for the region at
																				// 0,0

		// Runs for each element in the avgRGB 2D array to check for regions
		for (int r = 0; r < avgRGB.length; r++) {
			for (int c = 0; c < avgRGB[0].length; c++) {
				// Checks if this is a new region
				if (regionArray[r][c] == 0) {
					numRegions++;
					createRegionsRecursion(avgRGB, regionArray, r, c, r, c, numRegions);
				}
			}
		}
		return regionArray;
	}

	/**
	 * Changes array to label one entire region.
	 * 
	 * @param avgRGB    is the 2D array of average RGB values of pixels whose
	 *                  regions will be created for
	 * @param array     is the regions 2D array
	 * @param r         is the row of the element currently checking
	 * @param c         is the column of the element currently checking
	 * @param prevR     is the row of the element that the recursion came from
	 * @param prevC     is the column of the element that the recursion came from
	 * @param numRegion is the region number of this region
	 */
	private void createRegionsRecursion(int[][] avgRGB, int[][] array, int r, int c, int prevR, int prevC,
			int numRegion) {
		// Checks that this current r,c is part of the region of prevR, prevC
		if (Math.abs(avgRGB[r][c] - avgRGB[prevR][prevC]) <= regionRange) {
			array[r][c] = numRegion;

			// Runs for neighbors of r, c except prevR and prevC
			for (int num = -1; num <= 1; num += 2) {
				if (inBounds(avgRGB, r, c + num) && !((r == prevR) && (c + num == prevC)) && array[r][c + num] == 0) {
					createRegionsRecursion(avgRGB, array, r, c + num, r, c, numRegion);
				}
				if (inBounds(avgRGB, r + num, c) && !((r + num == prevR) && (c == prevC)) && array[r + num][c] == 0) {
					createRegionsRecursion(avgRGB, array, r + num, c, r, c, numRegion);
				}
			}
		}
	}

	/**
	 * Returns whether a given row and column is in bounds within a 2D array of
	 * integers
	 * 
	 * @param mat is the 2D array of integers
	 * @param r   is the row of the point checked
	 * @param c   is the column of the point checked
	 * @return whether this r, c combo is within bounds of mat
	 */
	private static boolean inBounds(int[][] mat, int r, int c) {
		return r >= 0 && r < mat.length && mat[r] != null && c >= 0 && c < mat[r].length;
	}

	/**
	 * Checks and returns a list of files that contain a spot where the temperature
	 * is significantly different than the temperature of spots around it.
	 * 
	 * @return a readable String of the files that needs to be manually checked.
	 */
	public String check() {
		try {
			ArrayList<String> output = new ArrayList<String>();
			// Runs a for loop to check each photo
			for (String photo : regions.keySet()) {
				if (checkHelper(regions.get(photo))) {
					output.add(photo);
				}
			}
			return getString(output);
		} catch (Exception E) {
			System.out.println("Something went wrong in checking.");
		}
		return "";
	}

	/**
	 * Checks each element's region to see if they're surrounded by another region
	 * 
	 * @param array is the 2D array of regions that will be checked
	 * @return true if this 2D array contains a region that is surrounded by another
	 *         region
	 */
	private boolean checkHelper(int[][] array) {
		// Creates an ArrayList of Integers for the regions we have already checked
		ArrayList<Integer> regionsChecked = new ArrayList<Integer>();

		// Runs for each element in the array
		for (int r = 0; r < array.length; r++) {
			for (int c = 0; c < array[0].length; c++) {
				// Makes sure that this element is neighboring another region before checking
				int temp = array[r][c];
				int temp2 = array[r][c];
				for (int num = -1; num <= 1; num += 2) {
					if (inBounds(array, r + num, c) && array[r + num][c] != temp2) {
						temp2 = array[r + num][c];
						break;
					}
					if (inBounds(array, r, c + num) && array[r][c + num] != temp2) {
						temp2 = array[r][c + num];
						break;
					}
				}
				// If this element is neighboring another region
				if (temp2 != temp) {
					if (noRecur) { // Uses a HashSet instead of recursion
						if (checkEach(array, temp, temp2, regionsChecked))
							return true;
					} else if (checkRecursion(array, r, c, -1, -1, temp, temp2, regionsChecked)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Checks if this region is inside another region by going through every point
	 * in the 2D array
	 * 
	 * @param array          is the 2D array of the region
	 * @param r              is the row of the element currently checking
	 * @param c              is the column of the element currently checking
	 * @param regionCurrent  is the current region tested to see if regionChecking
	 *                       is surrounding it
	 * @param regionChecking is the region that will be tested to see if it is
	 *                       surrounding regionCurrent
	 * @param regionsChecked is an ArrayList of the regions already checked
	 * @return true if the region is surrounded by another region, false otherwise
	 */
	private boolean checkEach(int[][] array, int regionCurrent, int regionChecking, ArrayList<Integer> regionsChecked) {
		// If we've already checked this region
		if (regionsChecked.contains(regionCurrent)) {
			return false;
		}
		// Runs for every point in the 2D array
		for (int r = 0; r < array.length; r++) {
			for (int c = 0; c < array[0].length; c++) {
				if (array[r][c] == regionCurrent) { // This point is part of region regionCurrent
					// If the region touches an edge, we'll say it's not something to be concerned
					// about since photos usually include the roof and surrounding places
					if (r == 0 || r == array.length - 1 || c == 0 || c == array[0].length - 1) {
						regionsChecked.add(regionCurrent);
						return false;
					}

					// Checks neighbors
					for (int i = -1; i <= 1; i += 2) {
						if (inBounds(array, r + i, c)) {
							if (array[r + i][c] != regionChecking) { // There is another region
								regionsChecked.add(regionCurrent);
								return false;
							}
						}
						if (inBounds(array, r, c + i)) {
							if (array[r][c + i] != regionChecking) { // There is another region
								regionsChecked.add(regionCurrent);
								return false;
							}
						}
					}
				}
			}
		}
		return true;
	}

	/**
	 * Checks if this region is inside another region recursively.
	 * 
	 * @param array          is the 2D array of the region
	 * @param r              is the row of the element currently checking
	 * @param c              is the column of the element currently checking
	 * @param prevR          is the row of the element that the recursion came from
	 * @param prevC          is the column of the element that the recursion came
	 *                       from
	 * @param regionCurrent  is the current region tested to see if regionChecking
	 *                       is surrounding it
	 * @param regionChecking is the region that will be tested to see if it is
	 *                       surrounding regionCurrent
	 * @param regionsChecked is an ArrayList of the regions already checked
	 * @return true if the region is surrounded by another region, false otherwise
	 */
	private boolean checkRecursion(int[][] array, int r, int c, int prevR, int prevC, int regionCurrent,
			int regionChecking, ArrayList<Integer> regionsChecked) {
		// If the region touches an edge, we'll say it's not something to be concerned
		// about since photos usually include the roof and surrounding places
		if (r == 0 || r == array.length - 1 || c == 0 || c == array[0].length - 1) {
			if (!regionsChecked.contains(regionCurrent)) {
				regionsChecked.add(regionCurrent);
			}
			return false;
		}
		// If we've already checked this region
		if (regionsChecked.contains(regionCurrent)) {
			return false;
		}
		// If this current region touches two different regions
		if (array[r][c] != regionCurrent && array[r][c] != regionChecking) {
			if (!regionsChecked.contains(regionCurrent)) {
				regionsChecked.add(regionCurrent);
			}
			return false;
		}

		boolean returnBoolean = true;
		// Checks for neighboring elements
		for (int num = -1; num <= 1; num += 2) {
			// Makes sure neighboring element is in bounds and not what is checked before
			if (inBounds(array, r + num, c) && !((r + num == prevR) && (c == prevC))) {
				// Checks that the neighboring element is part of this element's region
				if (array[r + num][c] == regionCurrent
						&& !checkRecursion(array, r + num, c, r, c, regionCurrent, regionChecking, regionsChecked)) {
					returnBoolean = false;
				}

			}
			// Same as above except the column is changed (row is changed in above
			// situation)
			if (inBounds(array, r, c + num) && !((r == prevR) && (c + num == prevC))) {
				if (array[r][c + num] == regionCurrent
						&& !checkRecursion(array, r, c + num, r, c, regionCurrent, regionChecking, regionsChecked)) {
					returnBoolean = false;
				}
			}
		}
		return returnBoolean;
	}

	/**
	 * Checks if a file is in .jpg format.
	 * 
	 * @param file is a file from computer
	 * @return true if it is a .jpg file, false otherwise
	 */
	private static boolean checkJPG(File file) {
		if (file == null) {
			throw new IllegalArgumentException("Input cannot be null.");
		}

		try {
			// Puts the file's name into a string
			String fileName = file.getName();

			// Stores the last few characters until there is a '.'
			int dotIndex = fileName.lastIndexOf('.');
			String type = (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);

			// Checks string if it s a .jpg
			if (type.equals("jpg")) {
				return true;
			} else {
				return false;
			}
		} catch (Exception E) {
			System.out.println("Something went wrong in checking if a file is a .jpg.");
		}
		return false;
	}

	/**
	 * Returns a human readable String out of an ArrayList of Strings
	 * 
	 * @param temp is an ArrayList<String>
	 * @return a String with every element in the ArrayList
	 */
	private String getString(ArrayList<String> temp) {
		if (temp == null) {
			throw new IllegalArgumentException("Input cannot be null");
		}

		// Saves time is size of temp is 0
		if (temp.size() == 0) {
			return "[]";
		}

		// Creates a StringBuilder that the program will add the Strings to
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (int i = 0; i < temp.size(); i++) {
			sb.append(temp.get(i) + ", ");
		}
		return sb.toString().substring(0, sb.toString().length() - 2) + "]";
	}

	/**
	 * @return all the file names stored in a String format
	 */
	public String returnAllStoredFiles() {
		// Saves time if no files are stored
		if (valRGB.size() == 0) {
			return "[]";
		}

		// Creates a StringBuilder with all files
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (String photo : valRGB.keySet()) {
			sb.append(photo + ", ");
		}
		return sb.toString().substring(0, sb.toString().length() - 2) + "]";
	}
}