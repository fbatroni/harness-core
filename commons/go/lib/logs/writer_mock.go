// Copyright 2020 Harness Inc. All rights reserved.
// Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
// that can be found in the licenses directory at the root of this repository, also available at
// https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.

// Code generated by MockGen. DO NOT EDIT.
// Source: writer.go

// Package logs is a generated GoMock package.
package logs

import (
	gomock "github.com/golang/mock/gomock"
	reflect "reflect"
)

// MockStreamWriter is a mock of StreamWriter interface.
type MockStreamWriter struct {
	ctrl     *gomock.Controller
	recorder *MockStreamWriterMockRecorder
}

// MockStreamWriterMockRecorder is the mock recorder for MockStreamWriter.
type MockStreamWriterMockRecorder struct {
	mock *MockStreamWriter
}

// NewMockStreamWriter creates a new mock instance.
func NewMockStreamWriter(ctrl *gomock.Controller) *MockStreamWriter {
	mock := &MockStreamWriter{ctrl: ctrl}
	mock.recorder = &MockStreamWriterMockRecorder{mock}
	return mock
}

// EXPECT returns an object that allows the caller to indicate expected use.
func (m *MockStreamWriter) EXPECT() *MockStreamWriterMockRecorder {
	return m.recorder
}

// Start mocks base method.
func (m *MockStreamWriter) Start() error {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "Start")
	ret0, _ := ret[0].(error)
	return ret0
}

// Start indicates an expected call of Start.
func (mr *MockStreamWriterMockRecorder) Start() *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "Start", reflect.TypeOf((*MockStreamWriter)(nil).Start))
}

// Write mocks base method.
func (m *MockStreamWriter) Write(p []byte) (int, error) {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "Write", p)
	ret0, _ := ret[0].(int)
	ret1, _ := ret[1].(error)
	return ret0, ret1
}

// Write indicates an expected call of Write.
func (mr *MockStreamWriterMockRecorder) Write(p interface{}) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "Write", reflect.TypeOf((*MockStreamWriter)(nil).Write), p)
}

// Open mocks base method.
func (m *MockStreamWriter) Open() error {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "Open")
	ret0, _ := ret[0].(error)
	return ret0
}

// Open indicates an expected call of Open.
func (mr *MockStreamWriterMockRecorder) Open() *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "Open", reflect.TypeOf((*MockStreamWriter)(nil).Open))
}

// Close mocks base method.
func (m *MockStreamWriter) Close() error {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "Close")
	ret0, _ := ret[0].(error)
	return ret0
}

// Close indicates an expected call of Close.
func (mr *MockStreamWriterMockRecorder) Close() *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "Close", reflect.TypeOf((*MockStreamWriter)(nil).Close))
}
