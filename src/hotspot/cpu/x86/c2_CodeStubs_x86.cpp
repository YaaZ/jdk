/*
 * Copyright (c) 2020, 2025, Oracle and/or its affiliates. All rights reserved.
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

#include "opto/c2_MacroAssembler.hpp"
#include "opto/c2_CodeStubs.hpp"
#include "runtime/objectMonitor.hpp"
#include "runtime/sharedRuntime.hpp"
#include "runtime/stubRoutines.hpp"

#define __ masm.

int C2SafepointPollStub::max_size() const {
  return 33;
}

void C2SafepointPollStub::emit(C2_MacroAssembler& masm) {
  assert(SharedRuntime::polling_page_return_handler_blob() != nullptr,
         "polling page return stub not created yet");
  address stub = SharedRuntime::polling_page_return_handler_blob()->entry_point();

  RuntimeAddress callback_addr(stub);

  __ bind(entry());
  InternalAddress safepoint_pc(masm.pc() - masm.offset() + _safepoint_offset);
  __ lea(rscratch1, safepoint_pc);
  __ movptr(Address(r15_thread, JavaThread::saved_exception_pc_offset()), rscratch1);
  __ jump(callback_addr);
}

int C2EntryBarrierStub::max_size() const {
  return 10;
}

void C2EntryBarrierStub::emit(C2_MacroAssembler& masm) {
  __ bind(entry());
  __ call(RuntimeAddress(StubRoutines::method_entry_barrier()));
  __ jmp(continuation(), false /* maybe_short */);
}

int C2FastUnlockLightweightStub::max_size() const {
  return 128;
}

void C2FastUnlockLightweightStub::emit(C2_MacroAssembler& masm) {
  assert(_t == rax, "must be");

  { // Restore lock-stack and handle the unlock in runtime.

    __ bind(_push_and_slow_path);
#ifdef ASSERT
    // The obj was only cleared in debug.
    __ movl(_t, Address(_thread, JavaThread::lock_stack_top_offset()));
    __ movptr(Address(_thread, _t), _obj);
#endif
    __ addl(Address(_thread, JavaThread::lock_stack_top_offset()), oopSize);
    // addl will always result in ZF = 0 (no overflows).
    __ jmp(slow_path_continuation());
  }
}

#undef __
