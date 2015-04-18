/*
 * The MIT License
 *
 * Copyright 2015 Maitha Manyala.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package ke.co.suncha.simba.admin.utils;

import java.util.Random;

/**
 * @author Maitha Manyala <maitha.manyala at gmail.com>
 *
 */
public class CreditCardNumberGenerator {
	private Random random = new Random(System.currentTimeMillis());

	/**
	 * Generates a random valid credit card number. For more information about
	 * the credit card number generation algorithms and credit card numbers
	 * refer to <a href="http://www.merriampark.com/anatomycc.htm">Anatomy of
	 * Credit Card Numbers</a>, <a
	 * href="http://euro.ecom.cmu.edu/resources/elibrary/everycc.htm">Everything
	 * you ever wanted to know about CC's</a>, <a
	 * href="http://www.darkcoding.net/credit-card/">Graham King's blog</a>, and
	 * <a href=
	 * "http://codytaylor.org/2009/11/this-is-how-credit-card-numbers-are-generated.html"
	 * >This is How Credit Card Numbers Are Generated</a>
	 * 
	 * @param bin
	 *            The bank identification number, a set digits at the start of
	 *            the credit card number, used to identify the bank that is
	 *            issuing the credit card.
	 * @param length
	 *            The total length (i.e. including the BIN) of the credit card
	 *            number.
	 * @return A randomly generated, valid, credit card number.
	 */
	public String generateRandomCreditCardNumber(String bin, int length) {

		// The number of random digits that we need to generate is equal to the
		// total length of the card number minus the start digits given by the
		// user, minus the check digit at the end.
		int randomNumberLength = length - (bin.length() + 1);

		StringBuffer buffer = new StringBuffer(bin);
		for (int i = 0; i < randomNumberLength; i++) {
			int digit = this.random.nextInt(10);
			buffer.append(digit);
		}

		// Do the Luhn algorithm to generate the check digit.
		int checkDigit = this.getCheckDigit(buffer.toString());
		buffer.append(checkDigit);

		return buffer.toString();
	}

	/**
	 * Generates the check digit required to make the given credit card number
	 * valid (i.e. pass the Luhn check)
	 * 
	 * @param number
	 *            The credit card number for which to generate the check digit.
	 * @return The check digit required to make the given credit card number
	 *         valid.
	 */
	private int getCheckDigit(String number) {

		// Get the sum of all the digits, however we need to replace the value
		// of every other digit with the same digit multiplied by 2. If this
		// multiplication yields a number greater than 9, then add the two
		// digits together to get a single digit number.
		//
		// The digits we need to replace will be those in an even position for
		// card numbers whose length is an even number, or those is an odd
		// position for card numbers whose length is an odd number. This is
		// because the Luhn algorithm reverses the card number, and doubles
		// every other number starting from the second number from the last
		// position.
		int sum = 0;
		int remainder = (number.length() + 1) % 2;
		for (int i = 0; i < number.length(); i++) {

			// Get the digit at the current position.
			int digit = Integer.parseInt(number.substring(i, (i + 1)));

			if ((i % 2) == remainder) {
				digit = digit * 2;
				if (digit > 9) {
					digit = (digit / 10) + (digit % 10);
				}
			}
			sum += digit;
		}

		// The check digit is the number required to make the sum a multiple of
		// 10.
		int mod = sum % 10;
		int checkDigit = ((mod == 0) ? 0 : 10 - mod);

		return checkDigit;
	}
}
