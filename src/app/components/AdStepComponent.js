import React from 'react';
import { useMyContext } from '@/app/context/myContext';
import Styles from '@/app/styles/PointDisplay.module.css';

const defaultTags = ['#한정특가', '#오늘단하루', '#점심타임딜', '#막차세일', '#반값찬스', '#골든타임특가', '#긴급할인', '#번개세일', '#타임어택',
'#초특가_1시간', '#지금이기회', '#순간최저가', '#마감임박할인', '#깜짝특가', '#득템찬스'];

const PointDisplay = () => {
  const { point } = useMyContext();

  return (
    <div className={Styles.pointContainer}>
      <div className={Styles.pointText}>
        <h2 className="text-lg font-bold">현재 보유 포인트</h2>
        <p className="text-2xl font-extrabold">{point}</p>
      </div>
      <div className={Styles.animationIcon}>
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="24" height="24">
          <defs>
            <linearGradient id="purple-yellow-gradient" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" stopColor="#8e2de2" stopOpacity="1" />
              <stop offset="100%" stopColor="#f1c40f" stopOpacity="1" />
            </linearGradient>
          </defs>
          <path fill="url(#purple-yellow-gradient)" d="M12,3C7.038,3,3,7.037,3,12s4.038,9,9,9,9-4.037,9-9S16.962,3,12,3Zm0,17c-4.411,0-8-3.589-8-8S7.589,4,12,4s8,3.589,8,8-3.589,8-8,8Zm3.495-10h-1.75l-.618-1.66c-.169-.472-.623-.789-1.126-.789s-.957.317-1.125.784l-.619,1.665h-1.75c-.503,0-.956.316-1.126.788-.168.465-.027.995.361,1.328l1.4,1.138-.554,1.777c-.158.488.012,1.021.42,1.324.173.131.5.225.72.245.242,0,.472-.068.667-.198l1.618-1.084,1.651,1.074c.431.273.988.25,1.375-.051.407-.302.574-.835.414-1.328l-.579-1.757,1.389-1.133s.007-.006.011-.009c.378-.327.519-.859.35-1.327-.17-.472-.623-.788-1.126-.788Zm.127,1.354l-1.648,1.345c-.161.131-.224.348-.159.544l.686,2.081c.026.081.002.169-.068.222-.067.051-.156.053-.229.006l-1.923-1.251c-.167-.108-.384-.107-.551.004l-1.892,1.266c-.017.012-.039.02-.062.025-.059-.005-.126-.007-.172-.041-.064-.048-.092-.137-.065-.221l.656-2.106c.061-.195-.003-.407-.162-.536l-1.653-1.342c-.065-.057-.09-.144-.062-.222.027-.074.105-.128.186-.128h2.098c.209,0,.396-.13.469-.326l.742-1.996c.055-.148.316-.153.374.005l.74,1.991c.073.196.26.326.469.326h2.098c.081,0,.159.054.186.128.028.077.004.17-.059.227ZM12,0C5.383,0,0,5.383,0,12s5.383,12,12,12,12-5.383,12-12S18.617,0,12,0Zm0,23c-6.065,0-11-4.935-11-11S5.935,1,12,1s11,4.935,11,11-4.935,11-11,11Z"/>
        </svg>
      </div>
    </div>
  );
};

const AdStepComponent = ({
  state,
  setStep,
  setContent,
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
  const { point, setPoint } = useMyContext();
  const calculateAdditionalCost = () => {
    if (state.startDate && state.endDate) {
      const start = new Date(state.startDate);
      const end = new Date(state.endDate);
      const diffInHours = Math.ceil((end - start) / (1000 * 60 * 60));
      return diffInHours > 2 ? (diffInHours - 2) * 10 : 0; 
    }
    return 0;
  };
  const handleRadiusChange = (value) => {
    setRadius(value);
    const costs = {
      '500m': 100,
      '1km': 200,
      '5km': 500,
      '10km': 1000
    };
    setPoint((prevPoint) => prevPoint - (costs[value] || 0));
  };

  const handleDatesChange = (startDate, endDate) => {
    setStartDate(startDate);
    setEndDate(endDate);
    const additionalCost = calculateAdditionalCost();
    setPoint((prevPoint) => prevPoint - additionalCost);
  };
  
  switch (state.step) {
    case 2:
      return (
        state.type === '광고' && (
          <div className="space-y-2">
            {state.selectedCategory === '유통' && (
              <div className="space-y-2">
                <div className="flex items-center w-full space-x-2">
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
                </div>
                <textarea
                    type="text"
                    className="w-full p-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-purple-500"
                    rows="3"
                    placeholder="세부 사항"
                    onChange={(e) => setContent(e.target.value)}
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
              <div className="flex flex-col space-y-2">
                <label className="text-sm">광고 전파 반경</label>
                  <select
                    onChange={(e) => setRadius(e.target.value)}
                    className="w-1/3 border rounded-lg p-2"
                  >
                    <option value="m">500m</option>
                    <option value="km">1km</option>
                    <option value="km">5km</option>
                    <option value="km">10km</option>
                  </select>
              </div>
              <div className="space-y-4">
                <div className="flex flex-col space-y-2">
                  <label htmlFor="start-datetime" className="text-sm">광고 시작일시</label>
                  <input
                    type="datetime-local"
                    id="start-datetime"
                    onChange={(e) => setStartDate(e.target.value)}
                    className="w-full h-12 border rounded-lg p-2"
                  />
                </div>
                <div className="flex flex-col space-y-2">
                  <label htmlFor="end-datetime" className="text-sm">광고 종료일시</label>
                  <input
                    type="datetime-local"
                    id="end-datetime"
                    onChange={(e) => setEndDate(e.target.value)}
                    className="w-full h-12 border rounded-lg p-2"
                  />
                </div>
              </div>
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
            <div className="space-y-2">
              <label className="block mb-2 text-sm text-gray-600">기본 태그 (최대 5개), 입력 태그(최대 10개)</label>
              <div className="flex flex-wrap">
                {defaultTags.map(tag => (
                <button
                  key={tag}
                  className={`p-2 m-1 rounded-md ${selectedDefaultTags.includes(tag) ? 'bg-purple-500 text-white' : 'bg-gray-200 text-gray-700'} focus:outline-none`}
                  onClick={() => handleDefaultTagClick(tag)}
                >
                  {tag}
                </button>
              ))}
            </div>
            <div className="flex flex-wrap ">
              {newTags.map(tag => (
                <div key={tag} className="flex items-center bg-purple-500 text-white p-2 m-1 rounded-md">
                  <span>{tag}</span>
                  <button onClick={() => removeNewTag(tag)} className="ml-2 text-xs">&times;</button>
                </div>
              ))}
            </div>
            <input
              type="text"
              className="w-full p-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-purple-500 mt-2"
              placeholder="새 태그를 입력하고 Enter 키를 누르세요"
              onKeyDown={handleNewTagInputKeyDown}
            />
          </div>
        </div>
        )
      );
    case 5:
      return (
        state.type === '광고' && (
          <div className="space-y-4">
            <PointDisplay />
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
