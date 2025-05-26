package desmoj.core.dist;

/**
 * Interface to create user-defined operators. Used by ContDistAggregate to combine distribution functions. Note that
 * implementations of common operators are available as static final fields.
 *
 * @author Peter Wueppen
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 */
public interface Operator {

    /**
     * Absolute difference operator, for arguments <code>a<code> and <code>b</code> returning <code>|a-b|</code>.
     */
    public static final Operator ABS_DIFF = new Operator() {
        @Override
        public String getDescription() {
            return "diff";
        }

        @Override
        public double result(double a, double b) {
            return Math.abs(a - b);
        }
    };

    /**
     * Divide operator, for arguments <code>a<code> and <code>b</code> returning
     * <code>a/b</code>.
     */
    public static final Operator DIVIDE = new Operator() {
        @Override
        public String getDescription() {
            return "divide";
        }

        @Override
        public double result(double a, double b) {
            return a / b;
        }
    };

    /**
     * Minus operator, for arguments <code>a<code> and <code>b</code> returning
     * <code>a-b</code>.
     */
    public static final Operator MINUS = new Operator() {
        @Override
        public String getDescription() {
            return "minus";
        }

        @Override
        public double result(double a, double b) {
            return a - b;
        }
    };

    /**
     * Multiply operator, for arguments <code>a<code> and <code>b</code> returning
     * <code>a-b</code>.
     */
    public static final Operator MULTIPLY = new Operator() {
        @Override
        public String getDescription() {
            return "muliply";
        }

        @Override
        public double result(double a, double b) {
            return a * b;
        }
    };

    /**
     * Plus operator, for arguments <code>a<code> and <code>b</code> returning
     * <code>a+b</code>.
     */
    public static final Operator PLUS = new Operator() {
        @Override
        public String getDescription() {
            return "plus";
        }

        @Override
        public double result(double a, double b) {
            return a + b;
        }
    };

    /**
     * Power operator, for arguments <code>a<code> and <code>b</code> returning
     * <code>a^b</code>.
     */
    public static final Operator POW = new Operator() {
        @Override
        public String getDescription() {
            return "pow";
        }

        @Override
        public double result(double a, double b) {
            return Math.pow(a, b);
        }
    };

    /**
     * Should return a description of what the operator does to be shown by the reporter. Example: "sum", "product"
     * etc.
     *
     * @returna a description
     */
    public String getDescription();

    /**
     * This should return the desired of the result or the operation, depending on the operands.
     *
     * @param operand1
     * @param operand2
     * @return the result
     */
    public double result(double operand1, double operand2);
}
