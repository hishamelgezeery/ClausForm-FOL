import java.awt.image.RescaleOp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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
	static boolean trace = false;

	public static void main(String[] args) {
		///////////////////Some Examples///////////////////////////////////////
		String test = "$ xy [ p(x) <=> q(x) ^ [ Q(x) ^ & y [ Q(y) ^ R(y,x) ] ] ] ";
		String testNOT = "[ ! ! p ] => [ ! ! q ]";
		test = " [ x | y ] ^ [ x | x ] ";
		String test3 = "[ ! ! p ] => [ ! ! q ]";
		String tst2 = "$x ! [ p(x) | q(x) ] ";
		String tst3 = "$x ! [ p(x) ^ [ q(x) ^ r(x) ] ]";
		String tst = "$ x ! [ p(x) ^ q(x) ] | [ r(s) ]";
		String tstrrr = "$ x ! [ p(x) | q(x) ]";
		String tstrrr2 = "$ x ! [ p(x) | q(x) ] | s(x)";
		String tstrrr3 = "$ x ! [ p(x) | q(x) | s(x) ]";
		String cnftranslation = "$ x [ p(x) | [ q(x) ^ r(x) ] ]";
		///////////////////////////// TEST CASES//////////////////////////////
		// //////////////////////////////////
		String testCase1 = "& x [P(x) ^ $ x [ Q(x) => ! P(x) ] ]";
		String testCase2 = "$ x [ P(x) <=> [ Q(x) ^ & y [ Q(y) ^ R(y,x) ] ] ]";
		AIProject a = new AIProject();
		a.trace = true;
		a.clauseForm(testCase1);
		a.clauseForm(testCase2);
		// ///////////////////////////////////////////////////////////////////////////

	}

	public void clauseForm(String FOLSentence) {

		// steps to convert to clause form

		String[] tempElements = FOLSentence.split(" ");
		ArrayList<String> elements = ArrayToArrayList(tempElements);

		if (trace) {

			System.out.print("Original Input: ");
			printArrayList(elements);
		}

		while (elements.contains(EQUIVALENCE)) {
			elements = removeEquiv(elements);
		}
		if (trace) {

			System.out.print("After Equivalence Elimination: ");
			printArrayList(elements);
		}

		while (elements.contains(IMPLICATION)) {
			elements = removeImplication(elements);
		}
		if (trace) {

			System.out.print("After Implication Elimination: ");
			printArrayList(elements);
		}
		while (!notInwardsFinished(elements)) {
			elements = pushNotInwards(elements);
		}
		if (trace) {

			System.out.print("After Pushing Not Inwards: ");
			printArrayList(elements);
		}

		if (elements.contains(FORALL) || elements.contains(THERE_EXISTS)) {
			elements = addVariables(elements);
		}
		if (trace) {

			System.out.print("After Standardizing Apart: ");
			printArrayList(elements);
		}

		if (elements.contains(THERE_EXISTS)) {
			elements = skolemize(elements);
		}
		if (trace) {

			System.out.print("After Skolemizing: ");
			printArrayList(elements);
		}

		elements = removeSpaces(elements);

		while (elements.contains(FORALL)) {
			elements = removeForAll(elements);
		}
		if (trace) {

			System.out.print("After Discarding For All Quantifier: ");
			printArrayList(elements);
		}

		while (!CNfTranslationNotPossible(elements)) {
			elements = CNFTransformation(elements);
		}
		if (trace) {

			System.out.print("After Translating Into CNF: ");
			printArrayList(elements);
		}

		// System.out.println(CNfTranslationNotPossible(elements));
		// System.out.println(CNFSuccessStartIndex(elements));
		// System.out.println(CNFTransformation(elements));

		elements = divideANDS(elements);
		elements = clauseFormI(elements);
		elements = clauseFormII(elements);
		elements = standarizeApart(elements);

		if (trace) {
			System.out
					.print("After flatten nested conjunctions and disjunctions: ");
			printArrayList(elements);

		}

		// printArrayList(elements);
		System.out.print("Final Result: ");
		printArrayList(elements);

	}

	public ArrayList<String> skolemize(ArrayList<String> inputString) {

		// saving all variables throughout the sentence
		ArrayList<String> allVariables = new ArrayList<String>();
		// saving original variables in certain quantifier
		ArrayList<String> originalValues = new ArrayList<String>();
		// saving replaced variables in certain quantifier
		ArrayList<String> replacementValues = new ArrayList<String>();

		ArrayList<String> temp;
		String variables;
		String[] tempElements;
		int start;
		// intialize dictionary for the quantifier
		boolean firstTime = true;

		while (inputString.contains(THERE_EXISTS)) {

			firstTime = true;
			originalValues.clear();
			replacementValues.clear();
			// look for 1st quantifier with code
			start = firstQuantifier(inputString, 2) + 1;
			if (start <= 0) {
				break;
			} else {
				// remove there_exists
				inputString.set(start - 1, "");
			}

			for (int startingIndex = start; startingIndex < inputString.size(); startingIndex++) {

				if (firstTime == true) {
					firstTime = false;
					variables = inputString.get(startingIndex);
					tempElements = variables.split("");
					replacementValues = ArrayToArrayList(tempElements);
					originalValues.addAll(replacementValues);
					for (int j = 0; j < replacementValues.size(); j++) {

						// look for an unused variable
						Random r = new Random();
						char c = (char) (r.nextInt(26) + 'A');
						String s = c + "";
						while (inputString.contains(s)
								|| allVariables.contains(s)
								|| replacementValues.contains(s)) {

							c = (char) (r.nextInt(26) + 'A');
							s = c + "";
						}
						replacementValues.set(j, s);

					}
					// merge the new variables
					inputString.set(startingIndex, "");

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
					inputString.set(startingIndex, all);

				}

			}
			// add the new variables to the global list and search for another
			// quantifier
			allVariables.addAll(replacementValues);

		}

		return inputString;
	}

	// calculating index of the 1st quantifier in arraylist
	public int firstQuantifier(ArrayList<String> inputString, int type) {
		// type=0 look for any quantifier
		// type=1 look for forAll
		// type=2 look for there_exits
		int temp;
		int firstQuantifier = -1;

		if (type == 0) {
			firstQuantifier = inputString.indexOf(FORALL);
			temp = inputString.indexOf(THERE_EXISTS);
			if (temp < firstQuantifier || firstQuantifier == -1) {
				firstQuantifier = temp;
			}
		} else if (type == 1) {
			firstQuantifier = inputString.indexOf(FORALL);
		} else if (type == 2) {
			firstQuantifier = inputString.indexOf(THERE_EXISTS);
		}

		return firstQuantifier;

	}

	// adding variables
	public ArrayList<String> addVariables(ArrayList<String> inputString) {

		// initialization for 1st quantifier

		ArrayList<String> editInput = new ArrayList<String>();
		ArrayList<String> allVariables = new ArrayList<String>();
		ArrayList<String> originalValues = new ArrayList<String>();
		ArrayList<String> replacementValues = new ArrayList<String>();

		ArrayList<String> temp;
		String variables;
		String[] tempElements;
		int start;
		int occurances = Collections.frequency(inputString, "$")
				+ Collections.frequency(inputString, "&");

		editInput.addAll(inputString);

		// intialize dictionary for the quantifier
		boolean firstTime = true;

		for (int i = 0; i < occurances; i++) {
			firstTime = true;
			originalValues.clear();
			replacementValues.clear();

			start = firstQuantifier(editInput, 0) + 1;
			if (start <= 0) {
				break;
			} else {
				editInput.set(start - 1, "+");
			}

			for (int startingIndex = start; startingIndex < inputString.size(); startingIndex++) {

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
									|| allVariables.contains(s)
									|| replacementValues.contains(s)) {
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

			}
			// add the new variables to the global list and search for another
			// quantifier
			allVariables.addAll(replacementValues);

		}

		return inputString;
	}

	private ArrayList<String> pushNotInwards(ArrayList<String> elements) {
		// case 1 - two nots followed by each other then remove both
		int size = elements.size() - 1;
		for (int i = 0; i < size; i++) {
			if (elements.get(i).equals(elements.get(i + 1))
					&& elements.get(i).equals(NOT)) {
				elements.remove(i);
				size--;
				elements.remove(i);
				size--;
			}
		}

		// case 2 - NOT followed by a bracketed expression e.g. ! [p V q]
		int notIndex = elements.indexOf(NOT);
		int lsqcount = 0;
		int rsqcount = 0;
		elements.remove(notIndex);
		notIndex--;
		int ptr = notIndex + 1;
		int iterations = 0;
		boolean flag = false;
		if (elements.get(ptr).equals(LEFT_SQUARE_BRACKET))
			lsqcount++;

		if (elements.get(ptr).equals(RIGHT_SQUARE_BRACKET))
			rsqcount++;

		int end = elements.size() - 1;
		while (ptr <= end && lsqcount != rsqcount) {
			if (iterations == 0) {
				flag = false;
				iterations++;
			}

			else
				flag = true;

			if (flag) {
				if (elements.get(ptr).equals(LEFT_SQUARE_BRACKET))
					lsqcount++;

				if (elements.get(ptr).equals(RIGHT_SQUARE_BRACKET))
					rsqcount++;
			}

			if (Character.isLetter(elements.get(ptr).charAt(0))) {
				elements.add(ptr, NOT);
				ptr++;
				end++;

			} else if (elements.get(ptr).equals(AND)) {
				elements.set(ptr, OR);
			}

			else if (elements.get(ptr).equals(OR)) {
				elements.set(ptr, AND);

			}

			else {
			}
			ptr++;
		}

		return elements;
	}

	// this method returns true if and only if all pushing inwards has been done
	// successfully
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

	// checks whether the given input is a Function or not
	private boolean isFunction(String s) {
		if (Character.isLetter(s.charAt(0)))
			return true;
		return false;
	}

	// checks whether the given input can be transformed using the rule:
	// [ P(x) | [ Q(x) ^ R(x) ] ] or alternatively
	// [ P(x) ^ [ Q(x) | R(x) ] ]
	private boolean CNFTransformationHelper(ArrayList<String> elements,
			int startindex) {
		if (elements.size() - startindex < 9 && startindex != 0)
			return false;

		else {
			if (elements.get(startindex).equals(LEFT_SQUARE_BRACKET)
					&& isFunction(elements.get(startindex + 1))
					&& (elements.get(startindex + 2).equals(OR) || elements
							.get(startindex + 2).equals(AND))
					&& elements.get(startindex + 3).equals(LEFT_SQUARE_BRACKET)
					&& isFunction(elements.get(startindex + 4))
					&& (elements.get(startindex + 5).equals(AND) || elements
							.get(startindex + 5).equals(AND))
					&& isFunction(elements.get(startindex + 6))
					&& elements.get(startindex + 7)
							.equals(RIGHT_SQUARE_BRACKET)
					&& elements.get(startindex + 8)
							.equals(RIGHT_SQUARE_BRACKET)
					&& !elements.get(startindex + 2).equals(
							elements.get(startindex + 5))) {
				return true;
			} else {
				return false;
			}
		}

	}

	// Uses the helper method above to detect such a sequence such that either
	// rules
	// [ P(x) | [ Q(x) ^ R(x) ] ] or alternatively
	// [ P(x) ^ [ Q(x) | R(x) ] ] can be transformed
	private boolean CNfTranslationNotPossible(ArrayList<String> elements) {
		boolean nomore = true;
		for (int i = 0; i < elements.size() - 1; i++) {
			if (CNFTransformationHelper(elements, i)) {
				return false;
			}
		}
		return nomore;
	}

	// Simply passes the very first index of the sequence needed in both
	// previous methods
	private int CNFSuccessStartIndex(ArrayList<String> elements) {
		for (int i = 0; i < elements.size() - 1; i++) {
			if (CNFTransformationHelper(elements, i)) {
				return i;
			}
		}
		return -1;
	}

	// This method does the CNFTransformation after the sequence has been
	// successfully detected
	// and the Transformation can start now
	private ArrayList<String> CNFTransformation(ArrayList<String> elements) {
		ArrayList<String> result = new ArrayList<String>();

		if (!CNfTranslationNotPossible(elements)) {
			int successIndex = CNFSuccessStartIndex(elements);

			for (int i = 0; i < successIndex; i++) {
				result.add(elements.get(i));
			}
			// [ P(x) | [ Q(x) ^ R(x) ] ]
			if (elements.get(successIndex + 5).equals(AND)) {
				result.add(LEFT_SQUARE_BRACKET);
				result.add(LEFT_SQUARE_BRACKET);
				result.add(elements.get(successIndex + 1));
				result.add(OR);
				result.add(elements.get(successIndex + 4));
				result.add(RIGHT_SQUARE_BRACKET);
				result.add(AND);
				result.add(LEFT_SQUARE_BRACKET);
				result.add(elements.get(successIndex + 1));
				result.add(OR);
				result.add(elements.get(successIndex + 6));
				result.add(RIGHT_SQUARE_BRACKET);
				result.add(RIGHT_SQUARE_BRACKET);
			}
			// [ P(x) ^ [ Q(x) | R(x) ] ]
			if (elements.get(successIndex + 5).equals(OR)) {
				result.add(LEFT_SQUARE_BRACKET);
				result.add(LEFT_SQUARE_BRACKET);
				result.add(elements.get(successIndex + 1));
				result.add(AND);
				result.add(elements.get(successIndex + 4));
				result.add(RIGHT_SQUARE_BRACKET);
				result.add(OR);
				result.add(LEFT_SQUARE_BRACKET);
				result.add(elements.get(successIndex + 1));
				result.add(AND);
				result.add(elements.get(successIndex + 6));
				result.add(RIGHT_SQUARE_BRACKET);
				result.add(RIGHT_SQUARE_BRACKET);
			}

		}
		return result;

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

	public ArrayList<String> removeForAll(ArrayList<String> inputString) {

		int firstForAll = firstQuantifier(inputString, 1);
		inputString.remove(firstForAll);
		inputString.remove(firstForAll);

		return inputString;
	}

	// replace square brackets with braces
	public ArrayList<String> addBraces(ArrayList<String> inputString) {

		while (inputString.contains(LEFT_SQUARE_BRACKET)
				|| inputString.contains(RIGHT_SQUARE_BRACKET)) {
			int left = inputString.indexOf(LEFT_SQUARE_BRACKET);
			int right = inputString.indexOf(RIGHT_SQUARE_BRACKET);

			if (left != -1) {
				inputString.set(left, "{");
			}
			if (right != -1) {
				inputString.set(right, "}");
			}

		}
		return inputString;
	}

	// replace AND and OR with commas
	public ArrayList<String> addCommas(ArrayList<String> inputString,
			String symbol) {
		while (inputString.contains(symbol)) {
			int symbolIndex = inputString.indexOf(symbol);
			if (symbolIndex != -1) {
				inputString.set(symbolIndex, ",");
			}

		}

		return inputString;
	}

	// divide array between ANDs
	public ArrayList<String> divideANDS(ArrayList<String> inputString) {

		while (inputString.contains(LEFT_SQUARE_BRACKET)
				|| inputString.contains(RIGHT_SQUARE_BRACKET)) {
			inputString.remove(LEFT_SQUARE_BRACKET);
			inputString.remove(RIGHT_SQUARE_BRACKET);
		}

		// look for each AND
		ArrayList<String> editOriginal = new ArrayList<String>();

		inputString.add(0, LEFT_SQUARE_BRACKET);
		editOriginal.addAll(inputString);

		while (editOriginal.contains(AND)) {
			int index = editOriginal.indexOf(AND);
			inputString.add(index + 1, LEFT_SQUARE_BRACKET);
			inputString.add(index, RIGHT_SQUARE_BRACKET);

			editOriginal.set(index, "+");
			editOriginal.add(index + 1, LEFT_SQUARE_BRACKET);
			editOriginal.add(index, RIGHT_SQUARE_BRACKET);
		}

		inputString.add(inputString.size(), RIGHT_SQUARE_BRACKET);

		return inputString;
	}

	// remove white spaces in array, used when removing forAll and There Exists
	public ArrayList<String> removeSpaces(ArrayList<String> inputString) {

		while (inputString.contains("")) {
			inputString.remove("");
		}

		return inputString;
	}

	public ArrayList<String> clauseFormI(ArrayList<String> inputString) {

		inputString = addBraces(inputString);
		inputString = addCommas(inputString, OR);

		return inputString;
	}

	public ArrayList<String> clauseFormII(ArrayList<String> inputString) {

		inputString.add(0, LEFT_CURLY_BRACE);
		inputString.add(inputString.size(), RIGHT_CURLY_BRACE);
		inputString = addCommas(inputString, AND);

		return inputString;
	}

	// checking that no two variables have the same name in same clause
	public ArrayList<String> standarizeApart(ArrayList<String> inputString) {

		ArrayList<String> allVariables = new ArrayList<String>();
		ArrayList<String> temporaryVariables = new ArrayList<String>();
		ArrayList<String> temp = null;

		int uniqness = 0;
		int countingBrackets = 0;

		for (int i = 0; i < inputString.size(); i++) {
			if (inputString.get(i).equals(LEFT_CURLY_BRACE)) {
				countingBrackets++;
			} else if (inputString.get(i).equals(RIGHT_CURLY_BRACE)) {
				countingBrackets--;
			} else if (countingBrackets == 1) {
				// when brackets are closed, new clause detected
				allVariables.addAll(temporaryVariables);
				temporaryVariables.clear();
				uniqness++;

			} else {
				// if function or variable replace or leave it accordingly
				String variables = inputString.get(i);
				String[] tempElements = variables.split("");
				temp = ArrayToArrayList(tempElements);

				for (int j = 0; j < temp.size(); j++) {
					if (temp.size() > 1) {
						if ((temp.get(j).matches(".*[a-z].*") || temp.get(j)
								.matches(".*[A-Z].*"))
								&& (!temp.get(j + 1).equals(LEFT_BRACE))) {
							temporaryVariables.add(temp.get(j));
						}
					} else if (temp.size() == 1) {
						if ((temp.get(j).matches(".*[a-z].*") || temp.get(j)
								.matches(".*[A-Z].*"))) {
							temporaryVariables.add(temp.get(j));
						}
					}

					if (allVariables.contains(temp.get(j))) {

						String letter = temp.get(j) + uniqness;
						temp.set(j, letter);
					}
				}
				String all = mergeArrayList(temp);
				inputString.set(i, all);
			}
		}

		return inputString;
	}

}
