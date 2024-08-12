"use client";
import React, { useState } from 'react';
import Styles from '@/app/styles/ReviewComponent.module.css'; 
import Stars from '@/app/styles/ReadTemplate.module.css';
import { FaStar } from 'react-icons/fa';

const Review = () => {
    const [reviewData, setReviewData] = useState({
        title: '',
        location: '',
        details: {
            date: '',
            review_type: '',
            rating: 0, // 평점의 기본값을 0으로 설정
            strengths: '',
            weaknesses: '',
            memorable_points: '',
            recomment_actions: '',
            revisit: '',
            additional_comments: '',
        }
    });

    // 별 클릭 시 호출되는 핸들러 함수
    const handleStarClick = (value) => {
        setReviewData(prevState => ({
            ...prevState,
            details: {
                ...prevState.details,
                rating: value
            }
        }));
    };

    // 별을 렌더링하는 함수
    const renderStars = (rating) => {
        const fullStars = Math.floor(rating);
        const hasHalfStar = rating % 1 !== 0;
        const totalStars = 5;
        return (
            <div className={Stars.StarRating}>
                {[...Array(totalStars)].map((_, index) => {
                    if (index < fullStars) {
                        return <span key={index} className={Stars.FilledStar}><FaStar onClick={() => handleStarClick(index + 1)} /></span>;
                    }
                    if (index === fullStars && hasHalfStar) {
                        return <span key={index} className={Stars.HalfStar}><FaStar onClick={() => handleStarClick(index + 1)} /></span>;
                    }
                    return <span key={index} className={Stars.EmptyStar}><FaStar onClick={() => handleStarClick(index + 1)} /></span>;
                })}
            </div>
        );
    };

    const handleChange = (e) => {
        const { name, value } = e.target;
        setReviewData(prevState => ({
            ...prevState,
            details: {
                ...prevState.details,
                [name]: value
            }
        }));
    };

    return (
        <div className={Styles.container}>
            <form className={Styles.form}>
                <div className='flex items-center w-full space-x-2'>
                    <div className={Styles.formGroup}>
                        <input
                            type="text"
                            id="title"
                            name="title"
                            placeholder='업소 이름'
                            className={Styles.input}
                            value={reviewData.title}
                            onChange={handleChange}
                            required
                        />
                    </div>

                    <div className={Styles.formGroup}>
                        <input
                            type="text"
                            id="location"
                            name="location"
                            placeholder='업소 위치'
                            value={reviewData.location}
                            className={Styles.input}
                            onChange={handleChange}
                            required
                        />
                    </div>
                </div>
                <div className='flex items-center w-full space-x-2'>
                    <div className={`${Stars.StarRating} flex-grow mb-2`} style={{ flex: '5' }}>
                        {renderStars(reviewData.details.rating)}
                    </div>
                    {/* <div className={Styles.formGroup} style={{ flex: '5' }}>    
                        <input
                            type="number"
                            id="rating"
                            name="rating"
                            placeholder='평점: 1~5점까지'
                            min="1"
                            max="5"
                            value={reviewData.details.rating}
                            onChange={handleChange}
                            required
                        />
                    </div> */}
                </div>

                <div className={Styles.formGroup}>
                    <textarea
                        id="strengths"
                        name="strengths"
                        placeholder='장점'
                        value={reviewData.details.strengths}
                        onChange={handleChange}
                        required
                    />
                </div>

                <div className={Styles.formGroup}>
                    <textarea
                        id="weaknesses"
                        name="weaknesses"
                        placeholder='단점'
                        value={reviewData.details.weaknesses}
                        onChange={handleChange}
                        required
                    />
                </div>

                <div className={Styles.formGroup}>
                    <textarea
                        id="memorablePoints"
                        name="memorablePoints"
                        placeholder='가장 기억에 남는 특징이나 경험'
                        value={reviewData.details.memorable_points}
                        onChange={handleChange}
                        required
                    />
                </div>

                <div className={Styles.formGroup}>
                    <textarea
                        id="recommendations"
                        name="recommendations"
                        placeholder='추천 대상'
                        value={reviewData.details.recomment_actions}
                        onChange={handleChange}
                        required
                    />
                </div>

                <div className={Styles.formGroup}>
                    <label htmlFor="revisit">재방문 의사</label>
                    <select
                        id="revisit"
                        name="revisit"
                        className={Styles.select}
                        value={reviewData.details.revisit}
                        onChange={handleChange}
                        required
                    >
                        <option value="">선택하세요</option>
                        <option value="예">예</option>
                        <option value="아니오">아니오</option>
                    </select>
                </div>

                <div className={Styles.formGroup}>
                    <label htmlFor="additionalComments">추가 코멘트</label>
                    <textarea
                        id="additionalComments"
                        name="additionalComments"
                        value={reviewData.details.additional_comments}
                        onChange={handleChange}
                    />
                </div>
            </form>
        </div>
    );
};

export default Review;
