/*
 * Copyright 2001-2014 Artima, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.scalatest.fixture

import scala.concurrent.{Promise, Future}
import org.scalatest._
import SharedHelpers.EventRecordingReporter
import org.scalatest.concurrent.SleepHelper

class AsyncFlatSpecLikeSpec2 extends org.scalatest.AsyncFunSpec {

  // SKIP-SCALATESTJS-START
  implicit val executionContext = scala.concurrent.ExecutionContext.Implicits.global
  // SKIP-SCALATESTJS-END
  //SCALATESTJS-ONLY implicit val executionContext = scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  override def newInstance = new AsyncFlatSpecLikeSpec2

  describe("AsyncFlatSpecLike") {

    it("can be used for tests that return Future") {

      class ExampleSpec extends AsyncFlatSpecLike {

        // SKIP-SCALATESTJS-START
        implicit val executionContext = scala.concurrent.ExecutionContext.Implicits.global
        // SKIP-SCALATESTJS-END
        //SCALATESTJS-ONLY implicit val executionContext = scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

        type FixtureParam = String
        def withAsyncFixture(test: OneArgAsyncTest): Future[Outcome] =
          test("testing")

        val a = 1

        it should "test 1" in { fixture =>
          Future {
            assert(a == 1)
          }
        }

        it should "test 2" in { fixture =>
          Future {
            assert(a == 2)
          }
        }

        it should "test 3" in { fixture =>
          Future {
            pending
          }
        }

        it should "test 4" in { fixture =>
          Future {
            cancel
          }
        }

        it should "test 5" ignore { fixture =>
          Future {
            cancel
          }
        }

        override def newInstance = new ExampleSpec
      }

      val rep = new EventRecordingReporter
      val spec = new ExampleSpec
      val status = spec.run(None, Args(reporter = rep))
      status.toFuture.map { s =>
        assert(rep.testStartingEventsReceived.length == 4)
        assert(rep.testSucceededEventsReceived.length == 1)
        assert(rep.testSucceededEventsReceived(0).testName == "should test 1")
        assert(rep.testFailedEventsReceived.length == 1)
        assert(rep.testFailedEventsReceived(0).testName == "should test 2")
        assert(rep.testPendingEventsReceived.length == 1)
        assert(rep.testPendingEventsReceived(0).testName == "should test 3")
        assert(rep.testCanceledEventsReceived.length == 1)
        assert(rep.testCanceledEventsReceived(0).testName == "should test 4")
        assert(rep.testIgnoredEventsReceived.length == 1)
        assert(rep.testIgnoredEventsReceived(0).testName == "should test 5")
      }
    }

    it("can be used for tests that did not return Future") {

      class ExampleSpec extends AsyncFlatSpecLike {

        // SKIP-SCALATESTJS-START
        implicit val executionContext = scala.concurrent.ExecutionContext.Implicits.global
        // SKIP-SCALATESTJS-END
        //SCALATESTJS-ONLY implicit val executionContext = scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

        type FixtureParam = String
        def withAsyncFixture(test: OneArgAsyncTest): Future[Outcome] =
          test("testing")

        val a = 1

        it should "test 1" in { fixture =>
          assert(a == 1)
        }

        it should "test 2" in { fixture =>
          assert(a == 2)
        }

        it should "test 3" in { fixture =>
          pending
        }

        it should "test 4" in { fixture =>
          cancel
        }

        it should "test 5" ignore { fixture =>
          cancel
        }

        override def newInstance = new ExampleSpec
      }

      val rep = new EventRecordingReporter
      val spec = new ExampleSpec
      val status = spec.run(None, Args(reporter = rep))
      status.toFuture.map { s =>
        assert(rep.testStartingEventsReceived.length == 4)
        assert(rep.testSucceededEventsReceived.length == 1)
        assert(rep.testSucceededEventsReceived(0).testName == "should test 1")
        assert(rep.testFailedEventsReceived.length == 1)
        assert(rep.testFailedEventsReceived(0).testName == "should test 2")
        assert(rep.testPendingEventsReceived.length == 1)
        assert(rep.testPendingEventsReceived(0).testName == "should test 3")
        assert(rep.testCanceledEventsReceived.length == 1)
        assert(rep.testCanceledEventsReceived(0).testName == "should test 4")
        assert(rep.testIgnoredEventsReceived.length == 1)
        assert(rep.testIgnoredEventsReceived(0).testName == "should test 5")
      }
    }

    it("should run tests that return Future in serial when oneAfterAnotherAsync is set to true") {

      @volatile var count = 0

      class ExampleSpec extends AsyncFlatSpecLike {

        override protected val oneAfterAnotherAsync: Boolean = true

        // SKIP-SCALATESTJS-START
        implicit val executionContext = scala.concurrent.ExecutionContext.Implicits.global
        // SKIP-SCALATESTJS-END
        //SCALATESTJS-ONLY implicit val executionContext = scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

        type FixtureParam = String
        def withAsyncFixture(test: OneArgAsyncTest): Future[Outcome] =
          test("testing")

        it should "test 1" in { fixture =>
          Future {
            SleepHelper.sleep(30)
            assert(count == 0)
            count = 1
            Succeeded
          }
        }

        it should "test 2" in { fixture =>
          Future {
            assert(count == 1)
            SleepHelper.sleep(50)
            count = 2
            Succeeded
          }
        }

        it should "test 3" in { fixture =>
          Future {
            assert(count == 2)
          }
        }

        override def newInstance = new ExampleSpec

      }

      val rep = new EventRecordingReporter
      val suite = new ExampleSpec
      val status = suite.run(None, Args(reporter = rep))
      status.toFuture.map { s =>
        assert(rep.testStartingEventsReceived.length == 3)
        assert(rep.testSucceededEventsReceived.length == 3)
      }
    }

    it("should run tests that does not return Future in serial when oneAfterAnotherAsync is set to true") {

      @volatile var count = 0

      class ExampleSpec extends AsyncFlatSpecLike {

        override protected val oneAfterAnotherAsync: Boolean = true

        // SKIP-SCALATESTJS-START
        implicit val executionContext = scala.concurrent.ExecutionContext.Implicits.global
        // SKIP-SCALATESTJS-END
        //SCALATESTJS-ONLY implicit val executionContext = scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

        type FixtureParam = String
        def withAsyncFixture(test: OneArgAsyncTest): Future[Outcome] =
          test("testing")

        it should "test 1" in { fixture =>
          SleepHelper.sleep(30)
          assert(count == 0)
          count = 1
          Succeeded
        }

        it should "test 2" in { fixture =>
          assert(count == 1)
          SleepHelper.sleep(50)
          count = 2
          Succeeded
        }

        it should "test 3" in { fixture =>
          assert(count == 2)
        }

        override def newInstance = new ExampleSpec

      }

      val rep = new EventRecordingReporter
      val suite = new ExampleSpec
      val status = suite.run(None, Args(reporter = rep))
      status.toFuture.map { s =>
        assert(rep.testStartingEventsReceived.length == 3)
        assert(rep.testSucceededEventsReceived.length == 3)
      }
    }

  }

}
