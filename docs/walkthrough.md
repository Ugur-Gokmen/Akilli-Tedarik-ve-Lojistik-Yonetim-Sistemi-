# QA Tests Implementation Walkthrough

I have successfully implemented all the QA tests outlined in the QA report into the `src/test/java` directory. These tests are now part of your project's test suite and can be executed via Maven.

## What Was Implemented

1. **State Transition Boundary Tests**
   - File: `ExtendedOrderStateTest.java`
   - Added a parameterized test checking all illegal state transitions (`Pending -> Ship`, `Approved -> Cancel`, etc.).
   - Verified that when an `IllegalStateException` is thrown, the original state of the `Order` is left completely intact (Atomic transition).

2. **Strategy Resolver Tests**
   - File: `PaymentStrategyResolverTest.java`
   - Tested the runtime strategy resolution mechanism with Mockito.
   - Asserts that correct dependencies are wired and that invalid payment strings throw security errors.

3. **Singleton Thread-Safety Tests**
   - File: `SystemLoggerSingletonTest.java`
   - Spun up 100 concurrent threads using `ExecutorService` mimicking a multi-user environment.
   - Confirmed that the `SystemLogger` does not suffer from race conditions and always returns the same memory reference.

4. **Observer Notification Tests**
   - File: `ProductObserverTest.java`
   - Mocks `StockObserver` and adds them to a product.
   - Drops stock below the threshold and uses `Mockito.verify(mock, times(1))` to confirm observers are notified.

5. **Cargo Adapter Boundary Tests**
   - File: `CargoAdaptersTest.java`
   - Tests boundary calculation on GlobalExpress logic.
   - Tests the system's reaction to invalid negative weights.

## Verification Results

I executed the test suite using `.\mvnw.cmd clean test`. 

> [!WARNING]
> **1 Bug Found!**
> As predicted in the QA report, the negative weight test failed: `java.lang.AssertionError: Expecting code to raise a throwable.`
> The application code (`YurticiCargoAdapter`) does not currently validate against negative weight inputs. This means our QA boundary test correctly identified a bug in the source code! 
> The rest of the tests (44 tests) passed perfectly.

You can view the specific failure inside the `target/surefire-reports` directory.
