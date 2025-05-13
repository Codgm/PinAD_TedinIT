"use client"
import React, { useState } from 'react';
import { FaStar } from 'react-icons/fa';
import Styles from '@/app/styles/ReadTemplate.module.css';

const FBTemplate = ({ data }) => {
  const fb = data || {
    title: "해피 식당",
    storeLocation: "서울특별시 강남구 역삼로 456",
    header: "여름 한정 시원한 냉면 할인!",
    discount: "20% OFF 냉면 전체 메뉴",
    schedule: {
      start: "2024-08-01",
      end: "2024-08-31"
    },
    products: [
      {
        name: "매운 냉면",
        price: "₩8,000",
        image: "https://example.com/spicy-noodle.jpg"
      },
      {
        name: "오리지널 냉면",
        price: "₩7,500",
        image: "https://example.com/original-noodle.jpg"
      }
    ],
    reviews: [
      {
        reviewer: "이수진",
        rating: 4.8,
        comment: "냉면이 정말 맛있고, 서비스도 훌륭했습니다. 꼭 다시 방문할게요!"
      },
      {
        reviewer: "박민수",
        rating: 4.2,
        comment: "가격 대비 훌륭한 맛입니다. 단, 대기 시간이 조금 길었어요."
      }
    ],
    ownerDescription: "해피 식당은 신선한 재료로 만든 냉면을 전문으로 합니다. 고객님들께 최고의 맛과 서비스를 제공하기 위해 최선을 다하고 있습니다.",
    details: "여름 한정으로 냉면 전체 메뉴를 20% 할인된 가격에 제공합니다. 시원하고 맛있는 냉면으로 더위를 식히세요!"
  };

  // 리뷰 표시 여부 상태
  const [showReviews, setShowReviews] = useState(true);

  // 평균 평점 계산 함수
  const calculateAverageRating = () => {
    if (fb.reviews.length === 0) return 0;
    const totalRating = fb.reviews.reduce((sum, review) => sum + review.rating, 0);
    return (totalRating / fb.reviews.length).toFixed(1);
  };

  // 별점 아이콘 렌더링 함수
  const renderStars = (rating) => {
    const fullStars = Math.floor(rating);
    const hasHalfStar = rating % 1 !== 0;
    const totalStars = 5;
  
    return (
      <div className={Styles.StarRating}>
        {[...Array(totalStars)].map((_, index) => {
          if (index < fullStars) {
            return <span key={index} className={Styles.FilledStar}><FaStar/></span>;
          }
          if (index === fullStars && hasHalfStar) {
            return <span key={index} className={Styles.HalfStar}><FaStar/></span>;
          }
          return <span key={index} className={Styles.EmptyStar}><FaStar/></span>;
        })}
      </div>
    );
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
        <h3 className={Styles.FBSubTitle}>관련 메뉴 목록</h3>
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
        <div className={Styles.AverageRating}>
          <h4>평균 평점: {calculateAverageRating()} / 5</h4>
          <div className={Styles.StarRating}>
            {renderStars(calculateAverageRating())}
          </div>
          <button 
            className={Styles.ToggleButton}
            onClick={() => setShowReviews(prev => !prev)}
          >
            {showReviews ? '리뷰 숨기기' : '리뷰 보기'}
          </button>
        </div>
        {showReviews && fb.reviews.map((review, index) => (
          <div key={index} className={Styles.FBReviewCard}>
            <p className={Styles.FBReviewerName}>{review.reviewer}</p>
            <div className={Styles.ReviewerRating}>
              {renderStars(review.rating)}
            </div>
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
