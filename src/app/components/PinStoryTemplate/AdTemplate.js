import React from 'react';
import Styles from '@/app/styles/ReadTemplate.module.css';

const AdTemplate = ({ data }) => {
  const ad = data || {
    title: "디지털 전자상가",
    storeLocation: "서울특별시 강남구 테헤란로 123",
    header: "스마트폰 대세 할인!",
    priceInfo: "최저가 ₩450,000부터 시작",
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
    media: [
      "https://example.com/video1.mp4",
      "https://example.com/photo1.jpg"
    ],
    storeInfo: {
      location: "서울특별시 강남구 테헤란로 123",
      contact: "전화: 02-1234-5678"
    },
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
    details: "이번 여름 한정으로 스마트폰 대세 할인! 다양한 스마트폰 모델을 최대 30%까지 할인된 가격에 제공합니다."
  };

  return (
    <div className={Styles.AdContainer}>
      {/* 업소 이름 및 위치 */}
      <div className={Styles.AdStoreInfo}>
        <h1 className={Styles.AdStoreName}>{ad.title}</h1>
        <p className={Styles.AdStoreLocation}>{ad.storeLocation}</p>
      </div>

      <div className={Styles.Header}>
        <h2 className={Styles.HeaderTitle}>{ad.header}</h2>
      </div>

      {/* 가격 정보 추가 */}
      <div className={Styles.PriceInfo}>
        <h3 className={Styles.SubTitle}>가격 정보</h3>
        <p>{ad.priceInfo}</p>
      </div>

      {/* 할인 혜택 */}
      <div className={Styles.Discount}>
        <h3 className={Styles.SubTitle}>할인 혜택</h3>
        <p>{ad.discount}</p>
      </div>

      {/* 할인 일정 */}
      <div className={Styles.Schedule}>
        <h3 className={Styles.SubTitle}>할인 일정</h3>
        <p>시작일: {ad.schedule.start}</p>
        <p>종료일: {ad.schedule.end}</p>
      </div>

      {/* 실사 영상 추가 */}
      <div className={Styles.Media}>
        <h3 className={Styles.SubTitle}>실사 영상</h3>
        <div className={Styles.MediaList}>
          {ad.media.map((item, index) => (
            item.endsWith('.mp4') && (
              <video key={index} controls className={Styles.MediaItem}>
                <source src={item} type="video/mp4" />
                Your browser does not support the video tag.
              </video>
            )
          ))}
        </div>
      </div>

      {/* 관련 상품 목록 */}
      <div className={Styles.Products}>
        <h3 className={Styles.SubTitle}>관련 상품 목록</h3>
        <div className={Styles.ProductList}>
          {ad.products.map((product, index) => (
            <div key={index} className={Styles.ProductCard}>
              <img src={product.image} alt={product.name} className={Styles.ProductImage} />
              <p className={Styles.ProductName}>{product.name}</p>
              <p className={Styles.ProductPrice}>{product.price}</p>
            </div>
          ))}
        </div>
      </div>

      {/* 매장 정보 변경 */}
      <div className={Styles.StoreInfo}>
        <h3 className={Styles.SubTitle}>매장 정보</h3>
        <p>위치: {ad.storeInfo.location}</p>
        <p>연락처: {ad.storeInfo.contact}</p>
      </div>

      {/* 리뷰 및 평점 추가 */}
      <div className={Styles.Reviews}>
        <h3 className={Styles.SubTitle}>리뷰 및 평점</h3>
        {ad.reviews.map((review, index) => (
          <div key={index} className={Styles.ReviewCard}>
            <p className={Styles.ReviewerName}>{review.reviewer}</p>
            <p className={Styles.ReviewerRating}>평점: {review.rating} / 5</p>
            <p className={Styles.ReviewerComment}>코멘트: {review.comment}</p>
          </div>
        ))}
      </div>

      {/* 세부 정보 변경 */}
      <div className={Styles.Details}>
        <h3 className={Styles.SubTitle}>세부 정보</h3>
        <p>{ad.details}</p>
      </div>
    </div>
  );
};

export default AdTemplate;
