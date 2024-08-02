import React, {useState} from 'react';
import { useMyContext } from '@/app/context/myContext';
import Styles from '@/app/styles/PointDisplay.module.css';
import MapExample from '@/app/components/mapExample';
import PointDisplay from './pointDisplay';
import PaymentSection from './paymentsection';

const defaultTags = ['#한정특가', '#오늘단하루', '#점심타임딜', '#막차세일', '#반값찬스', '#골든타임특가', '#긴급할인', '#번개세일', '#타임어택',
'#초특가_1시간', '#지금이기회', '#순간최저가', '#마감임박할인', '#깜짝특가', '#득템찬스'];

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
setSubscriptionDetails,
setReservationMethod,
}) => {
const { point, setPoint } = useMyContext();
const [markerPosition, setMarkerPosition] = useState({ top: 0, left: 0 });
const [maplocation, setmapLocation] = useState('');
const [timemoney, setTimemoney] = useState(0);
const [isPaymentRequired, setIsPaymentRequired] = useState(false);

const convertLocationToPosition = (location) => {
// 위치에 따라 좌표 변환 로직을 추가합니다. 여기에 임시 로직을 추가했습니다.
if (location === '서울') return { top: 100, left: 150 };
if (location === '강남') return { top: 200, left: 300 };
return { top: 0, left: 0 };
};

const handleLocationChange = (e) => {
const maplocation = e.target.value;
setmapLocation(maplocation);
const position = convertLocationToPosition(maplocation);
setMarkerPosition(position);
};

const calculateAdditionalCost = (startDate,endDate) => {
console.log(startDate)
console.log(endDate)
if (startDate && endDate) {
const start = new Date(startDate);
const end = new Date(endDate);
const diffInHours = Math.ceil((end - start) / (1000 * 60 * 60));
return diffInHours > 2 ? (diffInHours - 2) * 10 : 0;
}
return 0;
};

const handleRadiusChange = (value) => {

```
const costs = {
  '500m': 0,
  '1km': 100,
  '3km': 300,
  '5km': 500,
  '10km': 700
};

console.log(value.target.value)
const valuetemp = value.target.value
const cost = costs[valuetemp] || 0;
console.log(cost)
setRadius(cost);
if (point < cost) {
  setIsPaymentRequired(true);
} else {
  setIsPaymentRequired(false);
}

```

};

const handleDatesChange = (startDate, endDate) => {
console.log(startDate)
console.log(endDate)
setStartDate(startDate);
setEndDate(endDate);
const additionalCost = calculateAdditionalCost(startDate,endDate);
setTimemoney(additionalCost)
if (point < cost) {
setIsPaymentRequired(true);
} else {
setIsPaymentRequired(false);
}
};

switch (state.step) {
case 2:
return (
state.type === '광고' && (
<div className="space-y-2">
{state.selectedCategory === '유통' && (
<div className="space-y-2">
<div  className="flex items-center w-full space-x-2">
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
{state.selectedCategory === '요식업' && (
<div className="space-y-2">
<div className="flex items-center w-full space-x-2">
<input
type="text"
placeholder="식당 이름 입력"
onChange={(e) => setIndustryType(e.target.value)}
className="w-full h-12 border rounded-lg p-2"
/>
<input
type="text"
placeholder="운영 시간 입력"
onChange={(e) => setContent(e.target.value)}
className="w-full h-12 border rounded-lg p-2"
/>
</div>
<textarea
type="text"
className="w-full p-2 border rounded-lg"
rows="3"
placeholder="세부 사항 입력"
onChange={(e) => setContent(e.target.value)}
/>
<div className="flex flex-col space-y-4">
<label className="text-sm">메뉴 이름</label>
<input
type="text"
placeholder="메뉴 이름 입력"
onChange={(e) => setContent(prev => ({ ...prev, menuName: e.target.value }))}
className="w-full h-12 border rounded-lg p-2"
/>
</div>
<div className="flex flex-col space-y-4">
<label className="text-sm">메뉴 가격</label>
<input
type="text"
placeholder="메뉴 가격 입력"
onChange={(e) => setContent(prev => ({ ...prev, menuPrice: e.target.value }))}
className="w-full h-12 border rounded-lg p-2"
/>
</div>
<div className="flex flex-col space-y-4">
<label className="text-sm">메뉴 설명</label>
<textarea
className="w-full p-2 border rounded-lg"
rows="3"
placeholder="메뉴 설명 입력"
onChange={(e) => setContent(prev => ({ ...prev, menuDescription: e.target.value }))}
/>
</div>
<div className="flex flex-col space-y-4">
<label className="text-sm">예약 방법</label>
<select onChange={(e) => setReservationMethod(e.target.value)} className="w-full border rounded-lg p-2">
<option value="">선택</option>
<option value="전화">전화</option>
<option value="웹사이트">웹사이트</option>
<option value="앱">앱</option>
</select>
</div>
<div className="flex flex-col space-y-4">
<label className="text-sm">추가 혜택</label>
<input
type="text"
placeholder="예: 무료 음료, 디저트 제공"
onChange={(e) => setContent(prev => ({ ...prev, additionalBenefits: e.target.value }))}
className="w-full h-12 border rounded-lg p-2"
/>
</div>
<div className="flex flex-col space-y-4">
<label className="text-sm">예약 인원 수에 따른 할인</label>
<input
type="text"
placeholder="예: 4명 이상 예약 시 10% 할인"
onChange={(e) => setContent(prev => ({ ...prev, reservationDiscount: e.target.value }))}
className="w-full h-12 border rounded-lg p-2"
/>
</div>
<div className="flex flex-col space-y-4">
<label className="text-sm">특별 이벤트</label>
<input
type="text"
placeholder="예: 특정 날짜에 있는 특별 이벤트, 시즌별 프로모션"
onChange={(e) => setContent(prev => ({ ...prev, specialEvents: e.target.value }))}
className="w-full h-12 border rounded-lg p-2"
/>
</div>
<label className="text-sm">할인 형태 선택</label>
<select onChange={(e) => setDiscountType(e.target.value)} className="w-full border rounded-lg p-2">
<option value="">선택</option>
<option value="단체 할인">단체 할인</option>
</select>
{state.discountType === '단체 할인' && (
<div>
<label className="text-sm">단체 할인 설명</label>
<input
type="text"
placeholder="예: 2+1 할인"
onChange={(e) => setDiscountValue(e.target.value)}
className="w-full h-12 border rounded-lg p-2"
/>
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
<button onClick={() => removeNewTag(tag)} className="ml-2 text-xs">×</button>
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
case 4:
return (
state.type === '광고' && (
<div className="px-4 md:px-6 space-y-2">
<div className="flex flex-col space-y-4">
<PointDisplay loc={state.radius} time={timemoney}/>
{/* <label className="text-sm">광고 옵션 설정</label>
<select onChange={handleAdOptionChange} className="w-full border rounded-lg p-2">
<option value="">선택</option>
<option value="광고1">광고1</option>
<option value="광고2">광고2</option>
</select> */}
<div className="flex flex-col space-y-1">
<label className="text-sm">광고 전파 반경</label>
<select
onChange={handleRadiusChange}
className="w-1/3 border rounded-lg p-2"
>
<option value="500m">500m</option>
<option value="1km">1km</option>
<option value="3km">3km</option>
<option value="5km">5km</option>
<option value="10km">10km</option>
</select>
<p className="pl-1 text-sm text-gray-500">500m 무료 제공</p>
</div>
<div className="space-y-2">
<div className="flex flex-col space-y-2">
<label className="text-sm">광고 기간</label>
<div className="flex space-x-2">
<input
type="datetime-local"
onChange={(e) => handleDatesChange(e.target.value, state.endDate)}
className="w-1/2 border rounded-lg p-2"
/>
<input
type="datetime-local"
onChange={(e) => handleDatesChange(state.startDate, e.target.value)}
className="w-1/2 border rounded-lg p-2"
/>
</div>
</div>
<p className="pl-2 text-sm text-gray-500">2시간 이내 무료 제공</p>
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
onChange={handleLocationChange}
className="w-full h-12 border rounded-lg p-2"
/>
<MapExample markerPosition={markerPosition} setMarkerPosition={setMarkerPosition} />
<PaymentSection
setPayment={setPayment}
state={state}
isPaymentRequired={isPaymentRequired}
/>
</div>
</div>
)
);
// case 5:
//   return (
//     state.type === '광고' && (
//       <div className="space-y-4">
//         <PointDisplay />
//         <div className="space-y-4">
//           <div className="flex flex-col space-y-4">
//             <label className="text-sm">결제 정보 입력</label>

```
//             <select
//               value={state.paymentMode}
//               onChange={handlePaymentModeChange}
//               className="w-full h-12 border rounded-lg p-2"
//             >
//               <option value="one-time">일회성 구매</option>
//               <option value="subscription">정기구독</option>
//             </select>

//             {state.paymentMode === 'subscription' && (
//               <div className="mb-4">
//                 <label className="block mb-2">구독 주기 선택</label>
//                 <select
//                   value={state.subscriptionDetails.interval}
//                   onChange={(e) => setSubscriptionDetails({ interval: e.target.value })}
//                   className="w-full h-12 border rounded-lg p-2"
//                 >
//                   <option value="">구독 주기 선택</option>
//                   <option value="monthly">월간</option>
//                   <option value="yearly">연간</option>
//                 </select>
//               </div>
//             )}

//             {state.paymentMode === 'subscription' && state.subscriptionDetails.interval && (
//               <div className="mb-4">
//                 <label className="block mb-2">구독 금액 입력</label>
//                 <input
//                   type="number"
//                   placeholder="구독 금액 입력"
//                   onChange={(e) => setSubscriptionDetails({ amount: e.target.value })}
//                   value={state.subscriptionDetails.amount || ''}
//                   className="w-full h-12 border rounded-lg p-2"
//                 />
//               </div>
//             )}

//             {state.paymentMode === 'one-time' && (
//               <div className="mb-4">
//                 <label className="block mb-2">결제 금액 입력</label>
//                 <input
//                   type="number"
//                   placeholder="결제 금액 입력"
//                   onChange={(e) => setPayment(e.target.value)}
//                   value={state.payment || ''}
//                   className="w-full h-12 border rounded-lg p-2"
//                 />
//               </div>
//             )}

            // {/* 결제 방법 선택 */}
            // <div className="flex flex-col space-y-2">
            //   <label className="text-sm">결제 방법 선택</label>
            //   <select
            //     onChange={(e) => setPayment({ ...state.payment, method: e.target.value })}
            //     value={state.payment.method || ''}
            //     className="w-full border rounded-lg p-2"
            //   >
            //     <option value="">선택</option>
            //     <option value="신용카드">신용카드</option>
            //     <option value="계좌이체">계좌이체</option>
            //     <option value="간편결제">간편결제</option>
            //     <option value="기타">기타</option>
            //   </select>
            // </div>

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
//           </div>
//         </div>
          {/* <div className="flex flex-col space-y-2">
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
          </div> */}
  //     </div>
  //   )
  // );
default:
  return null;

```

}
};

export default AdStepComponent;