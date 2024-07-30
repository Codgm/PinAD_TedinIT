import React from 'react';
import { FaCamera, FaPen, FaTags, FaShare, FaCheck } from 'react-icons/fa';
import InstagramIcon from '/public/instagram_icon.svg';
import KakaoTalkIcon from '/public/kakaotalk_icon.svg';
import LineIcon from '/public/line_icon.svg';
import ThreadsIcon from '/public/threads_icon.svg';

const defaultTags = ['팝니다', '삽니다', '고칩니다', '알립니다'];

const AdStepComponent = ({
  state,
  setStep,
  handleCategoryChange,
  setSelectedCategory,
  setIndustryType,
  setStock,
  setDiscountType,
  setDiscountValue,
  setBundleType,
  setBundleNValue,
  setBundleDiscountValue,
  handleAdOptionChange,
  setRadius,
  setBoost,
  setLocation,
  setPayment,
  handleDefaultTagClick,
  handleNewTagInputKeyDown,
  removeNewTag,
  newTags,
  selectedDefaultTags,
  handlePlatformLinkChange,
  setStartDate,
  setEndDate,
  handlePaymentModeChange,
  setSubscriptionDetails
}) => {
  switch (state.step) {
    case 2:
      return (
        state.type === '광고' && (
          <div className="space-y-4">
            <select
              className="w-full border rounded-lg p-2"
              value={state.selectedCategory}
              onChange={(e) => setSelectedCategory(e.target.value)} 
            >
              <option value="">선택</option>
              <option value="유통">유통</option>
              <option value="요식업">요식업</option>
              <option value="이동형 판매">이동형 판매</option>
              <option value="서비스업">서비스업</option>
            </select>
            {state.selectedCategory === '유통' && (
              <div className="space-y-4">
                <input
                  type="text"
                  placeholder="상품명 입력"
                  onChange={(e) => setIndustryType(e.target.value)}
                  className="w-full h-12 border rounded-lg p-2"
                />
                <input
                  type="number"
                  placeholder="판매 수량 입력"
                  onChange={(e) => setStock(e.target.value)}
                  className="w-full h-12 border rounded-lg p-2"
                />
                <div className="flex flex-col space-y-4">
                  <label className="text-sm">할인 형태 선택</label>
                  <select onChange={(e) => setDiscountType(e.target.value)} className="w-full border rounded-lg p-2">
                    <option value="">선택</option>
                    <option value="단순 할인">단순 할인</option>
                    <option value="묶음 할인">묶음 할인</option>
                  </select>
                </div>
                {state.discountType === '단순 할인' && (
                  <input
                    type="text"
                    placeholder="할인율 또는 할인가 입력"
                    onChange={(e) => setDiscountValue(e.target.value)}
                    className="w-full h-12 border rounded-lg p-2"
                  />
                )}
                {state.discountType === '묶음 할인' && (
                  <div>
                    <label className="text-sm">묶음 할인 유형 선택</label>
                    <select onChange={(e) => setBundleType(e.target.value)} className="w-full border rounded-lg p-2">
                      <option value="">선택</option>
                      <option value="N+1">N+1</option>
                    </select>
                    {state.bundleType === 'N+1' && (
                      <div>
                        <label className="text-sm">N 값 입력</label>
                        <input
                          type="text"
                          placeholder="N값 입력"
                          onChange={(e) => setBundleNValue(e.target.value)}
                          value={state.bundleNValue}
                          className="w-full h-12 border rounded-lg p-2"
                        />
                      </div>
                    )}
                  </div>
                )}
                <div className="space-y-4">
                  <div className="flex flex-col space-y-2">
                    <label htmlFor="start-date" className="text-sm">할인 시작일</label>
                    <input
                      type="date"
                      id="start-date"
                      placeholder="할인 시작일 설정"
                      onChange={(e) => setStartDate(e.target.value)}
                      className="w-full h-12 border rounded-lg p-2"
                    />
                  </div>
                  <div className="flex flex-col space-y-2">
                    <label htmlFor="end-date" className="text-sm">할인 종료일</label>
                    <input
                      type="date"
                      id="end-date"
                      placeholder="할인 종료일 설정"
                      onChange={(e) => setEndDate(e.target.value)}
                      className="w-full h-12 border rounded-lg p-2"
                    />
                  </div>
                </div>
              </div>
            )}
          </div>
        )
      );
    case 3:
      return (
        state.type === '광고' && (
          <div className="px-4 md:px-6 space-y-4">
            <div className="flex flex-col space-y-4">
              <label className="text-sm">광고 옵션 설정</label>
              <select onChange={handleAdOptionChange} className="w-full border rounded-lg p-2">
                <option value="">선택</option>
                <option value="광고1">광고1</option>
                <option value="광고2">광고2</option>
              </select>
              <input
                type="text"
                placeholder="알림 반경 입력"
                onChange={(e) => setRadius(e.target.value)}
                className="w-full h-12 border rounded-lg p-2"
              />
              <label className="text-sm">광고 부스트 사용 여부</label>
              <select onChange={(e) => setBoost(e.target.value)} className="w-full border rounded-lg p-2">
                <option value="">선택</option>
                <option value="사용">사용</option>
                <option value="사용 안함">사용 안함</option>
              </select>
              <input
                type="text"
                placeholder="지도에서 위치 선택"
                onChange={(e) => setLocation(e.target.value)}
                className="w-full h-12 border rounded-lg p-2"
              />
            </div>
          </div>
        )
      );
    case 4:
      return (
        state.type === '광고' && (
          <div className="space-y-4">
            <div className="flex flex-col space-y-4">
              <label className="text-sm">결제 정보 입력</label>
              
              <select
                value={state.paymentMode}
                onChange={handlePaymentModeChange}
                className="w-full h-12 border rounded-lg p-2"
              >
                <option value="one-time">일회성 구매</option>
                <option value="subscription">정기구독</option>
              </select>

              {state.paymentMode === 'subscription' && (
                <div className="mb-4">
                  <label className="block mb-2">구독 주기 선택</label>
                  <select
                    value={state.subscriptionDetails.interval}
                    onChange={(e) => setSubscriptionDetails({ interval: e.target.value })}
                    className="w-full h-12 border rounded-lg p-2"
                  >
                    <option value="">구독 주기 선택</option>
                    <option value="monthly">월간</option>
                    <option value="yearly">연간</option>
                  </select>
                </div>
              )}

              {state.paymentMode === 'subscription' && state.subscriptionDetails.interval && (
                <div className="mb-4">
                  <label className="block mb-2">구독 금액 입력</label>
                  <input
                    type="number"
                    placeholder="구독 금액 입력"
                    onChange={(e) => setSubscriptionDetails({ amount: e.target.value })}
                    value={state.subscriptionDetails.amount || ''}
                    className="w-full h-12 border rounded-lg p-2"
                  />
                </div>
              )}

              {state.paymentMode === 'one-time' && (
                <div className="mb-4">
                  <label className="block mb-2">결제 금액 입력</label>
                  <input
                    type="number"
                    placeholder="결제 금액 입력"
                    onChange={(e) => setPayment(e.target.value)}
                    value={state.payment || ''}
                    className="w-full h-12 border rounded-lg p-2"
                  />
                </div>
              )}

              {/* 결제 방법 선택 */}
              <div className="flex flex-col space-y-2">
                <label className="text-sm">결제 방법 선택</label>
                <select
                  onChange={(e) => setPayment({ ...state.payment, method: e.target.value })}
                  value={state.payment.method || ''}
                  className="w-full border rounded-lg p-2"
                >
                  <option value="">선택</option>
                  <option value="신용카드">신용카드</option>
                  <option value="계좌이체">계좌이체</option>
                  <option value="간편결제">간편결제</option>
                  <option value="기타">기타</option>
                </select>
              </div>
              
              {/* 결제 날짜 선택 */}
              <div className="flex flex-col space-y-2">
                <label className="text-sm">결제 날짜</label>
                <input
                  type="date"
                  onChange={(e) => setPayment({ ...state.payment, date: e.target.value })}
                  value={state.payment.date || ''}
                  className="w-full border rounded-lg p-2"
                />
              </div>
              
              {/* 결제 참고사항 */}
              <textarea
                placeholder="결제 참고사항 입력"
                onChange={(e) => setPayment({ ...state.payment, notes: e.target.value })}
                value={state.payment.notes || ''}
                className="w-full border rounded-lg p-2 h-24"
              />
            </div>
          </div>
        )
      );
    case 5:
      return (
        state.type === '광고' && (
          <div className="space-y-4">
            <label className="block mb-2 text-sm text-gray-600">SNS 공유</label>
            <div className="flex space-x-2">
              <button className="p-2 rounded bg-yellow-300 text-white">
                <KakaoTalkIcon className="w-6 h-6" />
              </button>
              <button className="p-2 rounded bg-green-300 text-white">
                <LineIcon className="w-6 h-6" />
              </button>
              <button className="p-2 rounded bg-pink-300 text-white">
                <InstagramIcon className="w-6 h-6" />
              </button>
              <button className="p-2 rounded bg-gray-300 text-white">
                <ThreadsIcon className="w-6 h-6" />
              </button>
            </div>
            <div className="flex flex-col space-y-2">
              <label className="block text-sm text-gray-600">YouTube 프로필</label>
              <input
                type="text"
                className="w-full border p-2 rounded-md focus:outline-none focus:ring-2 focus:ring-purple-500"
                placeholder="YouTube URL"
                value={state.platformLinks['youtube'] || ''}
                onChange={(e) => handlePlatformLinkChange(e, 'youtube')}
              />
              <label className="block text-sm text-gray-600">Instagram 프로필</label>
              <input
                type="text"
                className="w-full border p-2 rounded-md focus:outline-none focus:ring-2 focus:ring-purple-500"
                placeholder="Instagram URL"
                value={state.platformLinks['instagram'] || ''}
                onChange={(e) => handlePlatformLinkChange(e, 'instagram')}
              />
            </div>
          </div>
        )
      );
    default:
      return null;
  }
};

export default AdStepComponent;
