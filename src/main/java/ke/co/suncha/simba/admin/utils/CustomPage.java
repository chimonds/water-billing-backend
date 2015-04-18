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


/**
 * @author Maitha Manyala <maitha.manyala at gmail.com>
 *
 */
public class CustomPage {
	private boolean first;
	private boolean last;
	private int totalPages;
	private Long totalElements;
	private int numberOfElements;
	private Object content;
	/**
	 * @return the first
	 */
	public boolean isFirst() {
		return first;
	}
	/**
	 * @param first the first to set
	 */
	public void setFirst(boolean first) {
		this.first = first;
	}
	/**
	 * @return the last
	 */
	public boolean isLast() {
		return last;
	}
	/**
	 * @param last the last to set
	 */
	public void setLast(boolean last) {
		this.last = last;
	}
	/**
	 * @return the totalPages
	 */
	public int getTotalPages() {
		return totalPages;
	}
	/**
	 * @param totalPages the totalPages to set
	 */
	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}
	/**
	 * @return the totalElements
	 */
	public Long getTotalElements() {
		return totalElements;
	}
	/**
	 * @param totalElements the totalElements to set
	 */
	public void setTotalElements(Long totalElements) {
		this.totalElements = totalElements;
	}
	/**
	 * @return the numberOfElements
	 */
	public int getNumberOfElements() {
		return numberOfElements;
	}
	/**
	 * @param numberOfElements the numberOfElements to set
	 */
	public void setNumberOfElements(int numberOfElements) {
		this.numberOfElements = numberOfElements;
	}
	/**
	 * @return the content
	 */
	public Object getContent() {
		return content;
	}
	/**
	 * @param content the content to set
	 */
	public void setContent(Object content) {
		this.content = content;
	}	
}
