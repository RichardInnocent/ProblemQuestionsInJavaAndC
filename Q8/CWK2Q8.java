/** 
 *  @author Anonymous (do not change)
 *  
 *  Question 8: 
 *  
 *  You are given the following information, but you may prefer 
 *  to do some research for yourself.
 *   •	1 Jan 1900 was a Monday.
 *   •	Thirty days has September, April, June and November. All the rest 
 *      have thirty-one, saving February alone, which has twenty-eight, rain 
 *      or shine. And on leap years, twenty-nine.
 *   •	A leap year occurs on any year evenly divisible by 4, but not on a 
 *      century unless it is divisible by 400.
 *      
 *  How many Tuesdays fell on the first of the month during the twentieth 
 *  century (1 Jan 1901 to 31 Dec 2000)?
 *      
 *  Note, this problem is inspired by Project Euler so, as stated in the 
 *  rules of Project Euler, your solution should return an answer under 
 *  60 seconds.
*/

public class CWK2Q8 {

	public static int howManyTuesdays() {
		// There are two implementations of this counter - one of which uses the java.time library, and
		// the other uses a "homemade" date. This uses the "homemade" version, but I've included the
		// other version with the submission for interest.
		return new HomemadeTuesdayCounter().countTuesdays();
	}
	
	public static void main(String[] args) {
		int result = CWK2Q8.howManyTuesdays();
		System.out.println("Number of Tuesdays = " + result);
	}
}
