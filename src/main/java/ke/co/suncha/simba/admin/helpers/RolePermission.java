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
package ke.co.suncha.simba.admin.helpers;

import java.util.List;

import ke.co.suncha.simba.admin.models.SystemAction;

/**
 * @author Maitha Manyala <maitha.manyala at gmail.com>
 *
 */
public class RolePermission {
private List<SystemAction> available;
private List<SystemAction> assigned;
/**
 * @return the available
 */
public List<SystemAction> getAvailable() {
	return available;
}
/**
 * @param available the available to set
 */
public void setAvailable(List<SystemAction> available) {
	this.available = available;
}
/**
 * @return the assigned
 */
public List<SystemAction> getAssigned() {
	return assigned;
}
/**
 * @param assigned the assigned to set
 */
public void setAssigned(List<SystemAction> assigned) {
	this.assigned = assigned;
}

}
