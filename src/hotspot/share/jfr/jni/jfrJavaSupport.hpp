/*
 * Copyright (c) 2016, 2025, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 *
 */

#ifndef SHARE_JFR_JNI_JFRJAVASUPPORT_HPP
#define SHARE_JFR_JNI_JFRJAVASUPPORT_HPP

#include "jfr/jni/jfrJavaCall.hpp"
#include "utilities/exceptions.hpp"

class Klass;
class outputStream;
template <typename E> class GrowableArray;

class JfrJavaSupport : public AllStatic {
 public:
  static jobject local_jni_handle(const oop obj, JavaThread* t);
  static jobject local_jni_handle(const jobject handle, JavaThread* t);
  static void destroy_local_jni_handle(jobject handle);

  static jobject global_jni_handle(const oop obj, JavaThread* t);
  static jobject global_jni_handle(const jobject handle, JavaThread* t);
  static void destroy_global_jni_handle(jobject handle);

  static jweak global_weak_jni_handle(const oop obj, JavaThread* t);
  static jweak global_weak_jni_handle(const jobject handle, JavaThread* t);
  static void destroy_global_weak_jni_handle(jweak handle);

  static oop resolve(jobject obj);
  static oop resolve_non_null(jobject obj);
  static void notify_all(jobject obj, TRAPS);
  static void set_array_element(jobjectArray arr, jobject element, int index, JavaThread* t);

  // naked oop result
  static void call_static(JfrJavaArguments* args, TRAPS);
  static void call_special(JfrJavaArguments* args, TRAPS);
  static void call_virtual(JfrJavaArguments* args, TRAPS);

  static void set_field(JfrJavaArguments* args, TRAPS);
  static void get_field(JfrJavaArguments* args, TRAPS);
  static void new_object(JfrJavaArguments* args, TRAPS);

  // global jni handle result
  static void new_object_global_ref(JfrJavaArguments* args, TRAPS);
  static void get_field_global_ref(JfrJavaArguments* args, TRAPS);

  // local jni handle result
  static void new_object_local_ref(JfrJavaArguments* args, TRAPS);
  static void get_field_local_ref(JfrJavaArguments* args, TRAPS);

  static jstring new_string(const char* c_str, TRAPS);

  static jobject new_java_lang_Boolean(bool value, TRAPS);
  static jobject new_java_lang_Integer(jint value, TRAPS);
  static jobject new_java_lang_Long(jlong value, TRAPS);

  // fields
  static bool compute_field_offset(int &dest_offset,
                                   Klass* klass,
                                   Symbol* name_symbol,
                                   Symbol* signature_symbol,
                                   bool is_static = false,
                                   bool allow_super = false);

  // misc
  static Klass* klass(const jobject handle);
  static const char* c_str(jstring string, Thread* thread, bool c_heap = false);
  static const char* c_str(oop string, Thread* thread, bool c_heap = false);
  static void free_c_str(const char* str, bool c_heap);
  static Symbol** symbol_array(jobjectArray string_array, JavaThread* thread, intptr_t* result_size, bool c_heap = false);
  static jlongArray create_long_array(GrowableArray<jlong>* array, TRAPS);

  // exceptions
  static void throw_illegal_state_exception(const char* message, TRAPS);
  static void throw_illegal_argument_exception(const char* message, TRAPS);
  static void throw_internal_error(const char* message, TRAPS);
  static void throw_out_of_memory_error(const char* message, TRAPS);
  static void throw_class_format_error(const char* message, TRAPS);
  static void throw_runtime_exception(const char* message, TRAPS);

  static void load_jdk_jfr_module(TRAPS);
  static bool is_jdk_jfr_module_available();
  static bool is_jdk_jfr_module_available(outputStream* stream, TRAPS);

  static jlong jfr_thread_id(JavaThread* jt, jobject thread);
  static void exclude(JavaThread* jt, jobject thread);
  static void include(JavaThread* jt, jobject thread);
  static bool is_excluded(jobject thread);
  static void exclude(Thread* thread);
  static void exclude(JavaThread* jt, oop ref, jobject thread);
  static void include(Thread* thread);
  static void include(JavaThread* jt, oop ref, jobject thread);
  static bool is_excluded(Thread* thread);
  static bool on_thread_start(Thread* t);

  static jobject get_configuration(jobject clazz, TRAPS);
  static bool set_configuration(jobject clazz, jobject configuration, TRAPS);

  static bool is_instrumented(jobject clazz, TRAPS);

  // critical
  static void abort(jstring errorMsg, TRAPS);
  static void abort(const char* error_msg, bool dump_core = true);
  static void uncaught_exception(jthrowable throwable, JavaThread* t);

  // asserts
  DEBUG_ONLY(static void check_java_thread_in_vm(JavaThread* t);)
  DEBUG_ONLY(static void check_java_thread_in_native(JavaThread* t);)
  DEBUG_ONLY(static void check_java_thread_in_java(JavaThread* t););

  enum CAUSE {
    VM_ERROR,
    OUT_OF_MEMORY,
    STACK_OVERFLOW,
    RUNTIME_EXCEPTION,
    UNKNOWN,
    NOF_CAUSES
  };

  static CAUSE cause();

 private:
  static CAUSE _cause;
  static void set_cause(jthrowable throwable, JavaThread* t);
};

#endif // SHARE_JFR_JNI_JFRJAVASUPPORT_HPP
