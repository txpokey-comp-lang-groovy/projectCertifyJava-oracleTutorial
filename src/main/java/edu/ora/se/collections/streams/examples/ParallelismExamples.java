/*
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */ 


package edu.ora.se.collections.streams.examples ;


import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class ParallelismExamples {

    public static void main(String... args) {
        
        // Create sample data

        List<Person> roster = Person.createRoster();

        printRosterContents(roster);
        
        // 1. Average age of male members in parallel

        double average = getAverage(roster);

        print("Average age of male members in parallel: " +
            average);

        // 2. Concurrent reduction example

        List<Map.Entry<Person.Sex, List<Person>>> byGenderList = getListByGenderViaParallelStream(roster);

        print("Group members by gender:");
        reportOnListByGender(byGenderList);

        // 3. Examples of ordering and parallelism

        print("Examples of ordering and parallelism:");
        Integer[] intArray = {1, 2, 3, 4, 5, 6, 7, 8 };
        List<Integer> listOfIntegers =  Arrays.asList(intArray);

        print("listOfIntegers:");
        reportListOfSampleIntegers(listOfIntegers);

        print("listOfIntegers sorted in reverse order:");
        reverseListOfSampleIntegers(listOfIntegers);
        reportListOfSampleIntegers(listOfIntegers);

        print("Parallel stream");
        reportOnListOfSampleIntegersViaParallelStream(listOfIntegers);

        print("Another parallel stream:");
        reportOnListOfSampleIntegersViaParallelStream(listOfIntegers);

        print("With forEachOrdered:");
        reportComparingParallelForEachOrderedApproach(listOfIntegers);
        
        // 4. Example of interference
        executeExampleOfInterferenceConcept();
        
        // 5. Stateful lambda expressions examples
        
        List<Integer> serialStorage = new ArrayList<>();

        print("Serial stream:");
        exampleStatefulLambdaSideEffect(listOfIntegers, serialStorage);

        reportOnListOfSampleIntegersUsingForEachOrdered(serialStorage);

        print("Parallel stream:");
        List<Integer> parallelStorage = exampleStatefulLambdaSideEffectUsingSyncListPlusParallelStream(listOfIntegers);
        print("");

        reportOnListOfSampleIntegersUsingForEachOrdered(parallelStorage);
    }

    private static void print(String s) {
        System.out.println(s);
    }

    private static List<Integer> exampleStatefulLambdaSideEffectUsingSyncListPlusParallelStream(List<Integer> listOfIntegers) {
        List<Integer> parallelStorage = Collections.synchronizedList(
            new ArrayList<>());
        listOfIntegers
            .parallelStream()

            // Don't do this! It uses a stateful lambda expression.
            .map(e -> { parallelStorage.add(e); return e; })

            .forEachOrdered(e -> System.out.print(e + " "));
        return parallelStorage;
    }

    private static void exampleStatefulLambdaSideEffect(List<Integer> listOfIntegers, List<Integer> serialStorage) {
        listOfIntegers
            .stream()

            // Don't do this! It uses a stateful lambda expression.
            .map(e -> { serialStorage.add(e); return e; })

            .forEachOrdered(e -> System.out.print(e + " "));
        print("");
    }

    private static void reportOnListOfSampleIntegersUsingForEachOrdered(List<Integer> serialStorage) {
        serialStorage
            .stream()
            .forEachOrdered(e -> System.out.print(e + " "));
        print("");
    }

    private static void executeExampleOfInterferenceConcept() {
        List<String> listOfStrings = Arrays.asList("one", "two");

        try {

            // This will fail as the peek operation will attempt to add the
            // string "three" to the source after the terminal operation has
            // commenced.

            String concatenatedString = listOfStrings.stream()

                // Don't do this! Interference occurs here.
                .peek(s -> listOfStrings.add("three"))

                .reduce((a, b) -> a + " " + b)
                .get();

            print("Concatenated string: " + concatenatedString);

        } catch (Exception e) {
            printError("Exception caught: " + e.toString());
        }
    }

    private static void printError( String s2) {
        System.err.println(s2);
    }

    private static void reportComparingParallelForEachOrderedApproach(List<Integer> listOfIntegers) {
        listOfIntegers
            .parallelStream()
            .forEachOrdered(e -> System.out.print(e + " "));
        print("");
    }

    private static void reportOnListOfSampleIntegersViaParallelStream(List<Integer> listOfIntegers) {
        listOfIntegers
            .parallelStream()
            .forEach(e -> System.out.print(e + " "));
        print("");
    }

    private static void reverseListOfSampleIntegers(List<Integer> listOfIntegers) {
        Comparator<Integer> normal = Integer::compare;
        Comparator<Integer> reversed = normal.reversed();
        Collections.sort(listOfIntegers, reversed);
    }

    private static void reportListOfSampleIntegers(List<Integer> listOfIntegers) {
        listOfIntegers
            .stream()
            .forEach(e -> System.out.print(e + " "));
        print("");
    }

    private static List<Map.Entry<Person.Sex, List<Person>>> getListByGenderViaParallelStream(List<Person> roster) {
        ConcurrentMap<Person.Sex, List<Person>>
            byGenderParallel =
            roster
                .parallelStream()
                .collect(Collectors.groupingByConcurrent(Person::getGender));

        return new ArrayList<>(byGenderParallel.entrySet());
    }

    private static void reportOnListByGender(List<Map.Entry<Person.Sex, List<Person>>> byGenderList) {
        byGenderList
            .stream()
            .forEach(e -> {
                print("Gender: " + e.getKey());
                e.getValue()
                    .stream()
                    .map(Person::getName)
                    .forEach(f -> System.out.println(f)); });
    }

    private static double getAverage(List<Person> roster) {
        return roster
                .parallelStream()
                .filter(p -> p.getGender() == Person.Sex.MALE)
                .mapToInt(Person::getAge)
                .average()
                .getAsDouble();
    }

    private static void printRosterContents(List<Person> roster) {
        print("Contents of roster:");
        roster
            .stream()
            .forEach(p -> p.printPerson());
        print("");
    }
}

