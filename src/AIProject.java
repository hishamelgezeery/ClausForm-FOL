import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

public class AIProject {

	final static String FORALL = "$";
	final static String THERE_EXISTS = "&";
	final static String AND = "^";
	final static String OR = "|";
	final static String NOT = "!";
	final static String EQUIVALENCE = "<=>";
	final static String IMPLICATION = "=>";
	final static String LEFT_SQUARE_BRACKET = "[";
	final static String RIGHT_SQUARE_BRACKET = "]";
	final static String LEFT_BRACE = "(";
	final static String RIGHT_BRACE = ")";
	final static String RIGHT_CURLY_BRACE = "}";
	final static String LEFT_CURLY_BRACE = "{";

	public static void main(String[] args) {
		// Scanner sc = new Scanner(System.in);
		// System.out.println("Please enter the sentence!");
		// String FOLSentence = sc.nextLine();
		//
		String test = "$xy [ p(x) <=> q(x) ^ [ Q(x) ^ &y [ Q(y) ^ R(y, x) ] ] ] ";
		// /////////////////////////////
		// IMPLIES
		String testIMPLIES = "[ [ p(x) ] => q(x) ]";
		// ///////////////////////
		String testNOT = "[ ! ! p ] => [ ! ! q ]";

		// /////////////////////////
		// RENAMING
		String testVARIABLES = "$ xy [ x ^ y ] | & yst [ p(y) ^ s ^ x ] ^ & sy [ s ^ y ] | $ ts [ t ^ s ] ";
		String testVARIABLES2 = "$ x [ p(x) ^ $ x [ q(x) ^ p(x) ] ]";

		// /////////////////////////////
		// NOT
		String test2 = "[ [ p(x) ] => q(x) ]";
		String test3 = "[ ! ! p ] => [ ! ! q ]";
		String tst = "$x [ p(x) <=> q(x) ]";
		String tst2 = "$x ! [ p(x) | q(x) ] ";
		String tst3 = "$x ! [ p(x) ^ [ q(x) ^ r(x) ] ]";
		// //////////////////////////////
		AIProject a = new AIProject();
		a.clauseForm(testVARIABLES2);

	}

	public void clauseForm(String FOLSentence) {
		String[] tempElements = FOLSentence.split(" ");
		ArrayList<String> elements = ArrayToArrayList(tempElements);

		while (elements.contains(EQUIVALENCE)) {
			elements = removeEquiv(elements);
			printArrayList(elements);
		}

		while (elements.contains(IMPLICATION)) {
			elements = removeImplication(elements);
		}

		// while (!notInwardsFinished(elements)) {
		// elements = pushNotInwards(elements);
		// System.out.println("still here");
		// }
		while (elements.contains(IMPLICATION)) {
			elements = removeImplication(elements);
		}

		if (elements.contains(FORALL) || elements.contains(THERE_EXISTS)) {
			int firstQuantifier = firstQuantifier(elements);
			elements = addVariables(elements, firstQuantifier);
		}

		printArrayList(elements);

	}

	// calculating index of the 1st quantifier in arraylist
	private int firstQuantifier(ArrayList<String> inputString) {
		int temp;
		int firstQuantifier = -1;

		if (inputString.contains(FORALL)) {

			firstQuantifier = inputString.indexOf(FORALL);
		}

		if (inputString.contains(THERE_EXISTS)) {
			temp = inputString.indexOf(THERE_EXISTS);

			if (temp < firstQuantifier || firstQuantifier == -1) {
				firstQuantifier = temp;
			}

		}
		return firstQuantifier;

	}

	// adding variables
	private ArrayList<String> addVariables(ArrayList<String> inputString,
			int indexFirstQuantifier) {

		// initialization for 1st quantifier

		ArrayList<String> editInput = new ArrayList<String>();
		ArrayList<String> allVariables = new ArrayList<String>();
		editInput.addAll(inputString);
		editInput.set(indexFirstQuantifier, "+");
		String variables = inputString.get(indexFirstQuantifier + 1);

		// divide in case of functions or variables like "xyz"

		String[] tempElements = variables.split("");
		ArrayList<String> temp = ArrayToArrayList(tempElements);

		// variables for comparison
		allVariables.addAll(temp);

		int occurances = Collections.frequency(inputString, "$")
				+ Collections.frequency(inputString, "&");
		int startingIndex;

		// intialize dictionary for the quantifier
		boolean firstTime = true;
		// don't count the first bracket
		boolean firstBracket = true;

		for (int i = 0; i < occurances; i++) {
			firstTime = true;
			firstBracket = true;
			ArrayList<String> originalValues = new ArrayList<String>();
			ArrayList<String> replacementValues = new ArrayList<String>();

			startingIndex = firstQuantifier(editInput) + 1;
			if (startingIndex <= 0) {
				break;
			} else {
				editInput.set(startingIndex - 1, "+");
			}
			int countingBrackets = 1;
			while (countingBrackets != 0) {
				// setting the dictionary for the quantifier parameters
				if (firstTime == true) {
					firstTime = false;
					variables = inputString.get(startingIndex);
					tempElements = variables.split("");
					replacementValues = ArrayToArrayList(tempElements);
					originalValues.addAll(replacementValues);
					for (int j = 0; j < replacementValues.size(); j++) {
						if (allVariables.contains(replacementValues.get(j))) {
							// look for an unused variable
							Random r = new Random();
							char c = (char) (r.nextInt(26) + 'a');
							String s = c + "";
							while (inputString.contains(s)
									&& allVariables.contains(s)) {
								c = (char) (r.nextInt(26) + 'a');
								s = c + "";
							}
							replacementValues.set(j, s);

						}

					}
					// merge the new variables
					String all = mergeArrayList(replacementValues);
					editInput.set(startingIndex, all);
					inputString.set(startingIndex, all);

				} else if (firstBracket) {
					// skip 1st bracket already counted
					firstBracket = false;
				} else if (editInput.get(startingIndex).equals(FORALL)
						|| editInput.get(startingIndex).equals(THERE_EXISTS)) {

					// skip if encountering sub-quantifiers
					startingIndex = skipSubQuantifier(countingBrackets,
							startingIndex, editInput);

				} else if (editInput.get(startingIndex).equals(
						RIGHT_SQUARE_BRACKET)) {
					countingBrackets--;
				} else if (editInput.get(startingIndex).equals(
						LEFT_SQUARE_BRACKET)) {
					countingBrackets++;
				} else {
					// if function or variable replace or leave it accordingly
					variables = inputString.get(startingIndex);
					tempElements = variables.split("");
					temp = ArrayToArrayList(tempElements);

					for (int j = 0; j < temp.size(); j++) {
						if (originalValues.contains(temp.get(j))) {

							String letter = temp.get(j);
							int location = originalValues.indexOf(letter);
							temp.set(j, replacementValues.get(location));
						}
					}

					String all = mergeArrayList(temp);
					editInput.set(startingIndex, all);
					inputString.set(startingIndex, all);

				}
				startingIndex++;

			}
			// add the new variables to the global list and search for another
			// quantifier
			allVariables.addAll(replacementValues);

		}

		return inputString;
	}

	public int skipSubQuantifier(int countingBrackets, int startingIndex,
			ArrayList<String> editInput) {
		int lastCount = countingBrackets;
		startingIndex = startingIndex + 3;
		lastCount = lastCount + 1;
		while (lastCount != startingIndex) {
			if (editInput.get(startingIndex).equals(RIGHT_SQUARE_BRACKET)) {
				lastCount--;

			} else if (editInput.get(startingIndex).equals(LEFT_SQUARE_BRACKET)) {
				lastCount++;
			}
			startingIndex++;
		}
		return startingIndex;
	}

	private ArrayList<String> pushNotInwards(ArrayList<String> elements) {
		// case 1 - two nots followed by each other - remove both
		ArrayList<String> twoNots = new ArrayList<String>(2);
		twoNots.add(NOT);
		twoNots.add(NOT);
		elements.removeAll(twoNots);

		// case 3 - NOT followed by a bracketed expression e.g. ! [p V q]
		int notIndex = elements.indexOf(NOT);
		int ptr = notIndex + 1;

		if (elements.get(notIndex + 2).equals(LEFT_SQUARE_BRACKET)) {
			System.out.println("hello");

			while (ptr < elements.size()
					&& (!elements.get(ptr).equals(RIGHT_SQUARE_BRACKET))) {

				if ((Character.isLetter(elements.get(ptr).charAt(0)))) {
					elements.add(ptr, NOT);
					ptr++;
				}

				if (elements.get(ptr).equals(AND)) {
					elements.set(ptr, OR);
					ptr++;
				}

				if (elements.get(ptr).equals(OR)) {
					elements.set(ptr, AND);
					ptr++;
				}

				ptr++;
			}

			/*
			 * while(!elements.get(ptr).equals(RIGHT_SQUARE_BRACKET) ||
			 * ptr<elements.size()){
			 * if(Character.isLetter(elements.get(ptr).charAt(0)))
			 * elements.add(ptr, NOT);
			 * 
			 * ptr++; }
			 */
		}
		// case 2 - not followed by a bracketed expression eg ! ( p v q )
		return elements;
	}

	private boolean notInwardsFinished(ArrayList<String> elements) {
		boolean finished = true;
		for (int i = 0; i < elements.size() - 1; i++) {
			if (elements.get(i).equals(NOT)
					&& !Character.isLetter(elements.get(i + 1).charAt(0))) {
				finished = false;
			}
		}
		return finished;
	}

	private ArrayList<String> removeImplication(ArrayList<String> inputString) {
		int implicationIndex = inputString.indexOf(IMPLICATION);
		// case of two functions eg. p(x) => q(x)
		if (!inputString.get(implicationIndex - 1).equals(RIGHT_SQUARE_BRACKET)
				&& !inputString.get(implicationIndex + 1).equals(
						LEFT_SQUARE_BRACKET)) {
			inputString = removeImplCaseOneThree(inputString, implicationIndex);
		}
		// case of two bracketed parameters eg. [p(x)^q(x)]=>[q(y)^q(z)]
		if (inputString.get(implicationIndex - 1).equals(RIGHT_SQUARE_BRACKET)
				&& inputString.get(implicationIndex + 1).equals(
						LEFT_SQUARE_BRACKET)) {
			inputString = removeImplCaseTwoFour(inputString, implicationIndex);
		}
		// case of two bracketed parameters eg. [p(x)^q(x)]<=>[q(y)^q(z)]
		if (!inputString.get(implicationIndex - 1).equals(RIGHT_SQUARE_BRACKET)
				&& inputString.get(implicationIndex + 1).equals(
						LEFT_SQUARE_BRACKET)) {
			inputString = removeImplCaseOneThree(inputString, implicationIndex);
		}
		if (!inputString.get(implicationIndex + 1).equals(LEFT_SQUARE_BRACKET)
				&& inputString.get(implicationIndex - 1).equals(
						RIGHT_SQUARE_BRACKET)) {
			inputString = removeImplCaseTwoFour(inputString, implicationIndex);
		}

		return inputString;
	}

	private ArrayList<String> removeImplCaseOneThree(
			ArrayList<String> inputString, int implicationIndex) {
		inputString.set(implicationIndex, OR);
		inputString.add(implicationIndex - 1, NOT);
		return inputString;
	}

	private ArrayList<String> removeImplCaseTwoFour(
			ArrayList<String> inputString, int implicationIndex) {
		inputString.set(implicationIndex, OR);
		int index = implicationIndex - 1;
		while (!inputString.get(index).equals(LEFT_SQUARE_BRACKET)) {
			index--;
		}
		inputString.add(index, NOT);
		return inputString;
	}

	// REMOVE EQUIVELENCE/////////////////////////////
	public ArrayList<String> removeEquiv(ArrayList<String> inputString) {
		int equivIndex = inputString.indexOf(EQUIVALENCE);
		// case#1 of two functions eg. p(x) <=> q(x)
		if (!inputString.get(equivIndex - 1).equals(RIGHT_SQUARE_BRACKET)
				&& !inputString.get(equivIndex + 1).equals(LEFT_SQUARE_BRACKET)) {
			inputString = removeEquivCaseOne(inputString, equivIndex);
		}
		// case#2 of two bracketed parameters eg. [p(x)^q(x)]<=>[q(y)^q(z)]
		if (inputString.get(equivIndex - 1).equals(RIGHT_SQUARE_BRACKET)
				&& inputString.get(equivIndex + 1).equals(LEFT_SQUARE_BRACKET)) {
			inputString = removeEquivCaseTwo(inputString, equivIndex);
		}
		// case#3 of a fuunction iff a bracketed parameter eg.
		// p(x)<=>[q(y)^q(z)]
		if (!inputString.get(equivIndex - 1).equals(RIGHT_SQUARE_BRACKET)
				&& inputString.get(equivIndex + 1).equals(LEFT_SQUARE_BRACKET)) {
			inputString = removeEquivCaseThree(inputString, equivIndex);
		}
		// case#4: a bracketed parameter iff a function eg. [q(y)^q(z)]<=>p(x)
		if (!inputString.get(equivIndex + 1).equals(LEFT_SQUARE_BRACKET)
				&& inputString.get(equivIndex - 1).equals(RIGHT_SQUARE_BRACKET)) {
			inputString = removeEquivCaseFour(inputString, equivIndex);
		}

		return inputString;
	}

	public ArrayList<String> removeEquivCaseOne(ArrayList<String> inputString,
			int equivIndex) {
		int beginIndex = equivIndex - 1;
		String parameter1 = inputString.get(beginIndex);
		String parameter2 = inputString.get(beginIndex + 2);
		// put first parameter between brackets
		inputString.add(beginIndex - 1, LEFT_SQUARE_BRACKET);
		inputString.add(beginIndex + 4, RIGHT_SQUARE_BRACKET);
		inputString.set(equivIndex + 1, IMPLICATION);
		// add new implication rule of reverse of operands
		int newBeginIndex = equivIndex + 4;
		inputString.add(newBeginIndex, AND);
		inputString.add(newBeginIndex + 1, LEFT_SQUARE_BRACKET);
		inputString.add(newBeginIndex + 2, parameter2);
		inputString.add(newBeginIndex + 3, IMPLICATION);
		inputString.add(newBeginIndex + 4, parameter1);
		inputString.add(newBeginIndex + 5, RIGHT_SQUARE_BRACKET);
		return inputString;

	}

	public ArrayList<String> removeEquivCaseTwo(ArrayList<String> inputString,
			int equivIndex) {
		ArrayList<String> operand1 = new ArrayList<String>();
		ArrayList<String> operand2 = new ArrayList<String>();
		int index = equivIndex + 1;
		int beginIndex = -1;
		int endIndex = -1;
		while (!inputString.get(index).equals(RIGHT_SQUARE_BRACKET)) {
			operand2.add(inputString.get(index));
			index++;
		}
		endIndex = index;
		operand2.add(RIGHT_SQUARE_BRACKET);

		index = equivIndex - 1;
		while (!inputString.get(index).equals(LEFT_SQUARE_BRACKET)) {
			operand1.add(0, inputString.get(index));
			index--;
		}
		beginIndex = index;
		operand1.add(0, LEFT_SQUARE_BRACKET);
		ArrayList<String> temp = createEquivAlt(operand1, operand2);

		// removing old parameter

		inputString = removeIndicies(inputString, beginIndex, endIndex);
		inputString.addAll(beginIndex - 1, temp);
		return inputString;
	}

	// removing case p(x) <=> [q(x)]
	public ArrayList<String> removeEquivCaseThree(
			ArrayList<String> inputString, int equivIndex) {

		ArrayList<String> operand1 = new ArrayList<String>();
		operand1.add(inputString.get(equivIndex - 1));
		ArrayList<String> operand2 = new ArrayList<String>();

		int index = equivIndex + 1;
		int beginIndex = equivIndex - 1;
		int endIndex = -1;
		while (!inputString.get(index).equals(RIGHT_SQUARE_BRACKET)) {
			operand2.add(inputString.get(index));
			index++;
		}
		endIndex = index;
		operand2.add(RIGHT_SQUARE_BRACKET);
		ArrayList<String> temp = createEquivAlt(operand1, operand2);

		// removing old parameter
		inputString = removeIndicies(inputString, beginIndex, endIndex);

		inputString.addAll(beginIndex - 1, temp);
		printArrayList(inputString);
		return inputString;

	}

	// [p(x)]<==> q(x)
	public ArrayList<String> removeEquivCaseFour(ArrayList<String> inputString,
			int equivIndex) {
		ArrayList<String> operand1 = new ArrayList<String>();
		ArrayList<String> operand2 = new ArrayList<String>();
		operand2.add(inputString.get(equivIndex + 1));

		int index = equivIndex - 1;
		int endIndex = equivIndex + 1;
		int beginIndex = -1;
		while (!inputString.get(index).equals(LEFT_SQUARE_BRACKET)) {
			operand1.add(0, inputString.get(index));
			index--;
		}
		beginIndex = index;
		operand1.add(0, LEFT_SQUARE_BRACKET);
		ArrayList<String> temp = createEquivAlt(operand1, operand2);

		// removing old parameter
		inputString = removeIndicies(inputString, beginIndex, endIndex);

		inputString.addAll(beginIndex - 1, temp);
		System.out.println("sss");
		return inputString;

	}

	public ArrayList<String> removeIndicies(ArrayList<String> original,
			int beginIndex, int endIndex) {
		ArrayList<String> remove = new ArrayList<String>();

		for (int i = beginIndex; i <= endIndex; i++) {

			remove.add(original.get(i));

		}
		original.removeAll(remove);
		return original;
	}

	public ArrayList<String> createEquivAlt(ArrayList<String> operand1,
			ArrayList<String> operand2) {
		ArrayList<String> temp = new ArrayList<String>();
		temp.add(LEFT_SQUARE_BRACKET);

		// add first implication
		temp.add(LEFT_SQUARE_BRACKET);
		temp.addAll(operand1);
		temp.add(IMPLICATION);
		temp.addAll(operand2);
		temp.add(RIGHT_SQUARE_BRACKET);

		// and operator
		temp.add(AND);
		// add second implication
		temp.add(LEFT_SQUARE_BRACKET);
		temp.addAll(operand2);
		temp.add(IMPLICATION);
		temp.addAll(operand1);
		temp.add(RIGHT_SQUARE_BRACKET);
		// close first opening bracket
		temp.add(RIGHT_SQUARE_BRACKET);
		return temp;
	}

	public String mergeArrayList(ArrayList<String> arraylist) {
		String output = "";

		for (String e : arraylist) {
			output = output + e;
		}
		return output;
	}

	public void printArrayList(ArrayList<String> arraylist) {
		String output = "";

		for (String e : arraylist) {
			output = output + e + " ";
		}
		System.out.println(output);
	}

	public ArrayList<String> ArrayToArrayList(String[] array) {
		ArrayList<String> wordList = new ArrayList<String>();

		for (String e : array) {
			wordList.add(e);
		}
		return wordList;
	}

}
