import React from 'react';
import Styles from '@/app/styles/ReadTemplate.module.css';

const FBTemplate = ({ data }) => {
  const fb = data || {
    title: "디지털 전자상가",
    storeLocation: "서울특별시 강남구 테헤란로 123",
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
    reviews: [
      {
        reviewer: "홍길동",
        rating: 4.5,
        comment: "할인된 가격에 매우 만족합니다. 제품 상태도 좋고 서비스가 훌륭했습니다."
      },
      {
        reviewer: "김철수",
        rating: 4.0,
        comment: "제품 품질이 좋습니다. 다만, 배송이 조금 늦었습니다."
      }
    ],
    ownerDescription: "디지털 전자상가는 최신 전자 제품을 경쟁력 있는 가격에 제공합니다. 고객님께 최고의 쇼핑 경험을 제공하기 위해 노력하고 있습니다.",
    details: "이번 여름 한정으로 스마트폰 대세 할인! 다양한 스마트폰 모델을 최대 30%까지 할인된 가격에 제공합니다."
  };

  return (
    <div className={Styles.FBContainer}>
      {/* 업소 이름 및 위치 */}
      <div className={Styles.FBStoreInfo}>
        <h1 className={Styles.FBStoreName}>{fb.title}</h1>
        <p className={Styles.FBStoreLocation}>{fb.storeLocation}</p>
      </div>

      {/* 헤더 */}
      <div className={Styles.FBHeader}>
        <h2 className={Styles.FBHeaderTitle}>{fb.header}</h2>
      </div>

      {/* 할인 혜택 */}
      <div className={Styles.FBDiscount}>
        <h3 className={Styles.FBSubTitle}>할인 혜택</h3>
        <p>{fb.discount}</p>
      </div>

      {/* 할인 일정 */}
      <div className={Styles.FBSchedule}>
        <h3 className={Styles.FBSubTitle}>할인 일정</h3>
        <p>시작일: {fb.schedule.start}</p>
        <p>종료일: {fb.schedule.end}</p>
      </div>

      {/* 관련 상품 목록 */}
      <div className={Styles.FBProducts}>
        <h3 className={Styles.FBSubTitle}>관련 상품 목록</h3>
        <div className={Styles.FBProductList}>
          {fb.products.map((product, index) => (
            <div key={index} className={Styles.FBProductCard}>
              <img src={product.image} alt={product.name} className={Styles.FBProductImage} />
              <p className={Styles.FBProductName}>{product.name}</p>
              <p className={Styles.FBProductPrice}>{product.price}</p>
            </div>
          ))}
        </div>
      </div>

      {/* 리뷰 및 평점 */}
      <div className={Styles.FBReviews}>
        <h3 className={Styles.FBSubTitle}>리뷰 및 평점</h3>
        {fb.reviews.map((review, index) => (
          <div key={index} className={Styles.FBReviewCard}>
            <p className={Styles.FBReviewerName}>{review.reviewer}</p>
            <p className={Styles.FBReviewerRating}>평점: {review.rating} / 5</p>
            <p className={Styles.FBReviewerComment}>코멘트: {review.comment}</p>
          </div>
        ))}
      </div>

      {/* 업주 설명 */}
      <div className={Styles.FBOwnerDescription}>
        <h3 className={Styles.FBSubTitle}>업주 설명</h3>
        <p>{fb.ownerDescription}</p>
      </div>

      {/* 세부 정보 */}
      <div className={Styles.FBDetails}>
        <h3 className={Styles.FBSubTitle}>세부 정보</h3>
        <p>{fb.details}</p>
      </div>
    </div>
  );
};

export default FBTemplate;
