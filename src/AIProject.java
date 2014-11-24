import java.util.ArrayList;
import java.util.Arrays;
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

		String test = "$xy [ p(x) <=> q(x) ^ [ Q(x) ^ &y [ Q(y) ^ R(y, x) ] ] ] ";
		String test2 = "[ [ p(x) ] <=> q(x) ]";
		AIProject a = new AIProject();
		a.clauseForm(test2);

	}

	public ArrayList ArrayToArrayList(String[] array) {
		ArrayList<String> wordList = new ArrayList<String>();

		for (String e : array) {
			wordList.add(e);
		}
		return wordList;
	}

	public void clauseForm(String FOLSentence) {
		String[] tempElements = FOLSentence.split(" ");
		ArrayList<String> elements = ArrayToArrayList(tempElements);

		if (elements.contains(EQUIVALENCE)) {
			elements = removeEquiv(elements);
			printArrayList(elements);
		}

	}
	
	
	
	
	
	//REMOVE EQUIVELENCE/////////////////////////////

	public ArrayList<String> removeEquiv(ArrayList<String> inputString) {
		int equivIndex = inputString.indexOf(EQUIVALENCE);
		// case of two functions eg. p(x) <=> q(x)
		if (!inputString.get(equivIndex - 1).equals(RIGHT_SQUARE_BRACKET)
				&& !inputString.get(equivIndex + 1).equals(LEFT_SQUARE_BRACKET)) {
			inputString = removeEquivCaseOne(inputString, equivIndex);
		}
		// case of two bracketed parameters eg. [p(x)^q(x)]<=>[q(y)^q(z)]
		if (inputString.get(equivIndex - 1).equals(RIGHT_SQUARE_BRACKET)
				&& inputString.get(equivIndex + 1).equals(LEFT_SQUARE_BRACKET)) {
			inputString = removeEquivCaseTwo(inputString, equivIndex);
		}
		// case of two bracketed parameters eg. [p(x)^q(x)]<=>[q(y)^q(z)]
		if (!inputString.get(equivIndex - 1).equals(RIGHT_SQUARE_BRACKET)
				&& inputString.get(equivIndex + 1).equals(LEFT_SQUARE_BRACKET)) {
			inputString = removeEquivCaseThree(inputString, equivIndex);
		}
		if (!inputString.get(equivIndex + 1).equals(LEFT_SQUARE_BRACKET)
				&& inputString.get(equivIndex - 1).equals(RIGHT_SQUARE_BRACKET)) {
			inputString = removeEquivCaseFour(inputString, equivIndex);
		}

		return inputString;
	}

	
	
	
	public void getOperands(){
		
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
	//[p(x)]<==> q(x)
	public ArrayList<String> removeEquivCaseFour(
			ArrayList<String> inputString, int equivIndex) {
		ArrayList<String> operand1 = new ArrayList<String>();
		ArrayList<String> operand2 = new ArrayList<String>();
		operand2.add(inputString.get(equivIndex + 1));

		int index = equivIndex - 1;
		int endIndex = equivIndex + 1;
		int beginIndex = -1;
		while (!inputString.get(index).equals(LEFT_SQUARE_BRACKET)) {
			operand1.add(0,inputString.get(index));
			index--;
		}
		beginIndex = index;
		operand1.add(0,LEFT_SQUARE_BRACKET);
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


	
	public void printArrayList(ArrayList<String> arraylist) {
		String output = "";

		for (String e : arraylist) {
			output = output + e + " ";
		}
		System.out.println(output);
	}
}
