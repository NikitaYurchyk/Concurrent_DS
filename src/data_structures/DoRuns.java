package data_structures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CyclicBarrier;

import data_structures.implementation.CoarseGrainedList;
import data_structures.implementation.CoarseGrainedTree;
import data_structures.implementation.FineGrainedList;
import data_structures.implementation.FineGrainedTree;
/*
import data_structures.implementation.LazyList;
import data_structures.implementation.LazyTree;
*/

/**
 * Adds and then removes items from a {@link Sorted} data structure, by
 * splitting the items to add and items to remove into chunks, to be added and
 * removed multi-threaded.
 *
 * @param <T>
 *            item type to be used.
 */
public class DoRuns<T extends Comparable<T>> {

    /** Specifies the number of threads to use. */
    private final int nrThreads;
    /** Specifies the items to be added. */
    private final T[] itemsToAdd;
    /** Specifies the items to be removed. */
    private final T[] itemsToRemove;
    /**
     * If {@literal >} 0, the threads spend some CPU time in between add or
     * remove operations.
     */
    private final int workTime;
    private final boolean same_order;
    /** When set, the data structure is printed after adding the items. */
    private final boolean debug;

    /** The actual data structure. */
    private final Sorted<T> sorted;

    /**
     * Initializes this object, and instantiates the actual data structure to be
     * used.
     *
     * @param dataStructure
     *            specifies which kind of data structure to use
     * @param nrThreads
     *            specifies the number of threads to use
     * @param itemsToAdd
     *            specifies the items to add
     * @param itemsToRemove
     *            specifies the items to remove
     * @param workTime
     *            if {@literal >} 0, the threads spend some CPU time in between
     *            add or remove operations.
     * @param same_order
     *            when set, items are removed in the same order as inserted
     * @param debug
     *            when set, the data structure is printed after adding the items
     */
    public DoRuns(String dataStructure, int nrThreads, T[] itemsToAdd,
            T[] itemsToRemove, int workTime, boolean same_order, boolean debug) {
        this.nrThreads = nrThreads;
        this.itemsToAdd = itemsToAdd;
        this.itemsToRemove = itemsToRemove;
        this.workTime = workTime;
        this.same_order = same_order;
        this.debug = debug;

        // Determine and allocate the data structure to be used.

        if (dataStructure.equalsIgnoreCase(Main.CGL)) {
            sorted = new CoarseGrainedList<T>();
        } else if (dataStructure.equalsIgnoreCase(Main.CGT)) {
            sorted = new CoarseGrainedTree<T>();
        } else if (dataStructure.equalsIgnoreCase(Main.FGL)) {
            sorted = new FineGrainedList<T>();
        } else if (dataStructure.equalsIgnoreCase(Main.FGT)) {
            sorted = new FineGrainedTree<T>();
/*
        } else if (dataStructure.equalsIgnoreCase(Main.LL)) {
            sorted = new LazyList<T>();
        } else if (dataStructure.equalsIgnoreCase(Main.LT)) {
            sorted = new LazyTree<T>();
*/
        } else {
            sorted = null;
            Main.exitWithError();
        }
    }

    /**
     * Runs the test, by first creating the worker threads, then starting them,
     * and then waiting for them to finish.
     */
    public void runDataStructure() {
        int runCount = 0;
        long totalAddTime = 0;
        long totalRemoveTime = 0;
        long start, end;

        // for (int run = 0; run < 10; run++) {
        for (int run = 0; run < 5; run++) {
            ArrayList<WorkerThread<T>> workerAddThreads = new ArrayList<WorkerThread<T>>();
            CyclicBarrier barrierAdd = new CyclicBarrier(nrThreads);

            // Add phase
            int sz = itemsToAdd.length / nrThreads;
            for (int i = 0; i < nrThreads; i++) {
                T[] toAdd = Arrays.copyOfRange(itemsToAdd, i * sz, (i + 1) * sz);
                // T[] toRemove = Arrays.copyOfRange(itemsToRemove, i * sz,
                //         (i + 1) * sz);
                workerAddThreads.add(new WorkerThread<T>(i, sorted, toAdd, null,
                        workTime, barrierAdd, same_order, debug)); // , true, false));
            }

            // Start worker threads
            start = System.currentTimeMillis();

            for (WorkerThread<T> t : workerAddThreads) {
                t.start();
            }

            // Wait until add worker threads are finished
            for (WorkerThread<T> t : workerAddThreads) {
                try {
                    t.join();
                } catch (InterruptedException e) {
                    throw new Error("Unexpected InterruptedException. Should not happen.", e);
                }
            }
            end = System.currentTimeMillis();

            System.out.printf("add time:         %d ms\n", end - start);
            totalAddTime += end - start;

            // Remove phase
            // Uses same number of threads as the add phase

            ArrayList<WorkerThread<T>> workerRemoveThreads = new ArrayList<WorkerThread<T>>();
            CyclicBarrier barrierRemove = new CyclicBarrier(nrThreads);

            for (int i = 0; i < nrThreads; i++) {
                // T[] toAdd = Arrays.copyOfRange(itemsToAdd, i * sz, (i + 1) * sz);
                T[] toRemove = Arrays.copyOfRange(itemsToRemove, i * sz,
                        (i + 1) * sz);
                workerRemoveThreads.add(new WorkerThread<T>(i, sorted, null, toRemove,
                        workTime, barrierRemove, same_order, debug)); // , false, true));
            }

            // Start worker threads
            start = System.currentTimeMillis();

            for (WorkerThread<T> t : workerRemoveThreads) {
                t.start();
            }

            // Wait until worker remove threads are finished
            for (WorkerThread<T> t : workerRemoveThreads) {
                try {
                    t.join();
                } catch (InterruptedException e) {
                    throw new Error("Unexpected InterruptedException. Should not happen.", e);
                }
            }
            end = System.currentTimeMillis();

            System.out.printf("remove+work time: %d ms\n", end - start);
            totalRemoveTime += end - start;

            // Report result.
            ArrayList<T> result = sorted.toArrayList();
            if (result.size() > 0) {
                System.out.println("ERROR: " + result.toString());
                break;
            }

            runCount++;
        }
        if (runCount > 0) {
            System.out.printf("Work: %d items of %d microseconds would take sequential total of %f milliseconds\n",
                              itemsToAdd.length, workTime, (float) itemsToAdd.length * workTime / 1000.0);
            System.out.printf("Average add time:           %f ms\n", (float) totalAddTime / runCount);
            System.out.printf("Average remove + work time: %f ms\n", (float) totalRemoveTime / runCount);
            System.out.printf("Average total time:         %f ms\n", (float) (totalAddTime + totalRemoveTime) / runCount);
        }
    }
}
