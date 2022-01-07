// Copyright 2020 Harness Inc. All rights reserved.
// Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
// that can be found in the licenses directory at the root of this repository, also available at
// https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.

// Code generated by MockGen. DO NOT EDIT.
// Source: client.go

// Package grpc is a generated GoMock package.
package grpc

import (
	gomock "github.com/golang/mock/gomock"
	service "github.com/wings-software/portal/960-expression-service/src/main/proto/io/harness/expression/service"
	reflect "reflect"
)

// MockExpressionEvalClient is a mock of ExpressionEvalClient interface.
type MockExpressionEvalClient struct {
	ctrl     *gomock.Controller
	recorder *MockExpressionEvalClientMockRecorder
}

// MockExpressionEvalClientMockRecorder is the mock recorder for MockExpressionEvalClient.
type MockExpressionEvalClientMockRecorder struct {
	mock *MockExpressionEvalClient
}

// NewMockExpressionEvalClient creates a new mock instance.
func NewMockExpressionEvalClient(ctrl *gomock.Controller) *MockExpressionEvalClient {
	mock := &MockExpressionEvalClient{ctrl: ctrl}
	mock.recorder = &MockExpressionEvalClientMockRecorder{mock}
	return mock
}

// EXPECT returns an object that allows the caller to indicate expected use.
func (m *MockExpressionEvalClient) EXPECT() *MockExpressionEvalClientMockRecorder {
	return m.recorder
}

// CloseConn mocks base method.
func (m *MockExpressionEvalClient) CloseConn() error {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "CloseConn")
	ret0, _ := ret[0].(error)
	return ret0
}

// CloseConn indicates an expected call of CloseConn.
func (mr *MockExpressionEvalClientMockRecorder) CloseConn() *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "CloseConn", reflect.TypeOf((*MockExpressionEvalClient)(nil).CloseConn))
}

// Client mocks base method.
func (m *MockExpressionEvalClient) Client() service.ExpressionEvaulatorServiceClient {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "Client")
	ret0, _ := ret[0].(service.ExpressionEvaulatorServiceClient)
	return ret0
}

// Client indicates an expected call of Client.
func (mr *MockExpressionEvalClientMockRecorder) Client() *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "Client", reflect.TypeOf((*MockExpressionEvalClient)(nil).Client))
}
