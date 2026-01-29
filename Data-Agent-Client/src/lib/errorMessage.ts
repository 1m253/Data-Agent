import { ErrorCode, HttpStatusCode } from '../constants/errorCode';

interface ErrorResponseData {
    code?: number;
    message?: string;
}

interface ApiErrorLike {
    response?: {
        status?: number;
        data?: ErrorResponseData;
    };
    message?: string;
}

/**
 * 统一从后端错误中解析出用户可读的错误文案
 * @param error AxiosError 或任意 error 对象
 * @param fallback 默认兜底文案
 */
export function resolveErrorMessage(error: unknown, fallback: string): string {
    const err = error as ApiErrorLike | undefined;

    // 优先使用后端直接返回的 message
    const backendMessage = err?.response?.data?.message;
    if (backendMessage && typeof backendMessage === 'string') {
        return backendMessage;
    }

    const status = err?.response?.status;
    const code = err?.response?.data?.code;

    // 根据业务错误码做基础映射
    if (typeof code === 'number') {
        switch (code) {
            case ErrorCode.NOT_LOGIN_ERROR:
                return 'You are not logged in. Please sign in and try again.';
            case ErrorCode.NO_AUTH_ERROR:
                return 'You do not have permission to perform this action.';
            case ErrorCode.VALIDATION_ERROR:
            case ErrorCode.REQUIRED_FIELD_EMPTY:
            case ErrorCode.FIELD_FORMAT_ERROR:
            case ErrorCode.FIELD_LENGTH_EXCEEDED:
            case ErrorCode.FIELD_VALUE_OUT_OF_RANGE:
                return 'Some fields are invalid. Please check your input and try again.';
            case ErrorCode.DB_CONNECTION_ERROR:
            case ErrorCode.DB_CONNECTION_TIMEOUT:
                return 'Database connection issue. Please try again later.';
            case ErrorCode.SYSTEM_ERROR:
                return 'Server is busy now. Please try again later.';
            default:
                break;
        }
    }

    // 根据 HTTP 状态码做兜底映射
    if (typeof status === 'number') {
        switch (status) {
            case HttpStatusCode.BAD_REQUEST:
                return 'Request is invalid. Please check your input.';
            case HttpStatusCode.UNAUTHORIZED:
                return 'Authentication failed. Please sign in again.';
            case HttpStatusCode.FORBIDDEN:
                return 'You are not allowed to perform this action.';
            case HttpStatusCode.NOT_FOUND:
                return 'Requested resource was not found.';
            case HttpStatusCode.INTERNAL_SERVER_ERROR:
                return 'Server error. Please try again later.';
            default:
                break;
        }
    }

    // 兜底：用 error.message 或调用方给的 fallback
    if (err?.message && typeof err.message === 'string') {
        return err.message;
    }

    return fallback;
}

