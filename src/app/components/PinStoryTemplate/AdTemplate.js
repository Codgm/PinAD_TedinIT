import React from 'react';

const AdTemplate = ({ data }) => {
  const ad = data || {
    header: "스마트폰 대세 할인!",
    discount: "30% OFF 모든 스마트폰",
    schedule: {
      start: "2024-08-01",
      end: "2024-08-31"
    },
    products: [
      {
        name: "스마트폰 A",
        price: "₩500,000",
        image: "https://example.com/productA.jpg"
      },
      {
        name: "스마트폰 B",
        price: "₩600,000",
        image: "https://example.com/productB.jpg"
      }
    ],
    details: "이번 여름 한정으로 스마트폰 대세 할인! 다양한 스마트폰 모델을 최대 30%까지 할인된 가격에 제공합니다."
  };

  return (
    <div className="p-6">
      <div className="mb-4">
        <h2 className="text-xl font-semibold">{ad.header}</h2>
      </div>
      <div className="mb-4">
        <h3 className="text-lg font-semibold">할인 혜택</h3>
        <p>{ad.discount}</p>
      </div>
      <div className="mb-4">
        <h3 className="text-lg font-semibold">할인 일정</h3>
        <p>시작일: {ad.schedule.start}</p>
        <p>종료일: {ad.schedule.end}</p>
      </div>
      <div className="mb-4">
        <h3 className="text-lg font-semibold">관련 상품 목록</h3>
        <div className="flex flex-wrap">
          {ad.products.map((product, index) => (
            <div key={index} className="w-1/2 p-2">
              <img src={product.image} alt={product.name} className="w-full h-20 object-cover rounded-sm" />
              <p className="text-sm mt-1">{product.name}</p>
              <p className="text-sm">{product.price}</p>
            </div>
          ))}
        </div>
      </div>
      <div>
        <h3 className="text-lg font-semibold">세부 정보</h3>
        <p>{ad.details}</p>
      </div>
    </div>
  );
};

export default AdTemplate;
