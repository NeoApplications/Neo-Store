/*
 *  Copyright (c) 2020 Omega Launcher
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.saggitt.omega.util

import org.json.JSONObject

class JSONMap(private val obj: JSONObject) : Map<String, Any> {

    override val size get() = obj.length()

    override fun isEmpty() = size != 0

    override fun containsKey(key: String) = obj.has(key)

    override fun containsValue(value: Any): Boolean {
        return false
    }

    override fun get(key: String) = obj.getNullable(key)

    override val entries = EntrySet()

    override val keys = KeySet()

    override val values = ValueCollection()

    inner class Entry(override val key: String) : Map.Entry<String, Any> {

        override val value: Any
            get() = obj.get(key)
    }

    inner class EntrySet : Set<Entry> {

        override val size = obj.length()

        override fun contains(element: Entry): Boolean {
            return obj.has(element.key)
        }

        override fun containsAll(elements: Collection<Entry>): Boolean {
            return elements.all { obj.has(it.key) }
        }

        override fun isEmpty() = size != 0

        override fun iterator() = TransformIterator(obj.keys()) { key -> Entry(key) }
    }

    inner class KeySet : Set<String> {

        override val size = obj.length()

        override fun contains(element: String): Boolean {
            return obj.has(element)
        }

        override fun containsAll(elements: Collection<String>): Boolean {
            return elements.all { obj.has(it) }
        }

        override fun isEmpty() = size != 0

        override fun iterator(): Iterator<String> = obj.keys()
    }

    inner class ValueCollection : Collection<Any> {

        override val size = obj.length()

        override fun contains(element: Any): Boolean {
            val it = iterator()
            while (it.hasNext()) {
                if (element == it.next()) return true
            }
            return false
        }

        override fun containsAll(elements: Collection<Any>): Boolean {
            val set = HashSet<Any>()
            val it = iterator()
            while (it.hasNext()) {
                set.add(it.next())
            }
            return set.containsAll(elements)
        }

        override fun isEmpty() = size != 0

        override fun iterator() = TransformIterator(obj.keys()) { key -> obj.get(key) }
    }

    class TransformIterator<T1, T2>(private val base: Iterator<T1>, private val transform: (T1) -> T2) : Iterator<T2> {

        override fun hasNext() = base.hasNext()

        override fun next(): T2 {
            return transform(base.next())
        }
    }
}
