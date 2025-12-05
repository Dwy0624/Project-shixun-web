import request from '@/utils/request'

/**
 * 获取心理测评详情（包含题目）
 * @param {number} testId - 测评ID
 */
export const getPsychologicalTestDetail = (testId) => {
    return request({
        url: `/psychological-test/${testId}/detail`,
        method: 'get'
    })
}

/**
 * 获取测评基本信息
 * @param {number} testId - 测评ID
 */
export const getPsychologicalTestInfo = (testId) => {
    return request({
        url: `/psychological-test/${testId}/info`,
        method: 'get'
    })
}

/**
 * 获取测评报告
 * @param {number} reportId - 报告ID
 * @param {number} score - 测评得分
 */
export const getTestReport = (reportId, score) => {
    return request({
        url: `/psychological-test/report/${reportId}`,
        method: 'get',
        params: { score }
    })
}

/**
 * 提交测评答案
 * @param {Object} data - 提交的数据
 */
export const submitTestAnswers = (data) => {
    return request({
        url: '/psychological-test/submit',
        method: 'post',
        data
    })
}