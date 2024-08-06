"use client";
import React, { useState } from 'react';
import Styles from '@/app/styles/ReviewComponent.module.css'; 

const Review = () => {
    const [reviewData, setReviewData] = useState({
        placeName: '',
        visitDate: '',
        type: '',
        rating: '',
        strengths: '',
        weaknesses: '',
        memorablePoints: '',
        recommendations: '',
        revisit: '',
        additionalComments: '',
        industryType: '',
        stock: '',
        content: '',
        discountType: '',
        bundleType: '',
        bundleNValue: ''
    });

    const handleChange = (e) => {
        const { name, value } = e.target;
        setReviewData(prevState => ({
            ...prevState,
            [name]: value
        }));
    };

    return (
        <div className={Styles.container}>
            <form className={Styles.form}>
                <div className='flex items-center w-full space-x-2'>
                    <div className={Styles.formGroup}>
                        <label htmlFor="placeName">장소 이름</label>
                        <input
                            type="text"
                            id="placeName"
                            name="placeName"
                            className={Styles.input}
                            value={reviewData.placeName}
                            onChange={handleChange}
                            required
                        />
                    </div>

                    <div className={Styles.formGroup}>
                        <label htmlFor="visitDate">방문 날짜</label>
                        <input
                            type="date"
                            id="visitDate"
                            name="visitDate"
                            className={Styles.input}
                            value={reviewData.visitDate}
                            onChange={handleChange}
                            required
                        />
                    </div>
                </div>
                <div className='flex items-center w-full space-x-2'>
                    <div className={Styles.formGroup}>
                        <label htmlFor="type">종류</label>
                        <select
                            id="type"
                            name="type"
                            className={Styles.select}
                            value={reviewData.type}
                            onChange={handleChange}
                            required
                        >
                            <option value="">선택하세요</option>
                            <option value="식당">식당</option>
                            <option value="카페">카페</option>
                            <option value="관광지">관광지</option>
                            <option value="숙소">숙소</option>
                        </select>
                    </div>

                    <div className={Styles.formGroup}>
                        <label htmlFor="rating">평점</label>
                        <input
                            type="number"
                            id="rating"
                            name="rating"
                            placeholder='1~5점까지'
                            min="1"
                            max="5"
                            value={reviewData.rating}
                            onChange={handleChange}
                            required
                        />
                    </div>
                </div>

                <div className={Styles.formGroup}>
                    <label htmlFor="strengths">장점</label>
                    <textarea
                        id="strengths"
                        name="strengths"
                        value={reviewData.strengths}
                        onChange={handleChange}
                        required
                    />
                </div>

                <div className={Styles.formGroup}>
                    <label htmlFor="weaknesses">단점</label>
                    <textarea
                        id="weaknesses"
                        name="weaknesses"
                        value={reviewData.weaknesses}
                        onChange={handleChange}
                        required
                    />
                </div>

                <div className={Styles.formGroup}>
                    <label htmlFor="memorablePoints">인상적인 점</label>
                    <textarea
                        id="memorablePoints"
                        name="memorablePoints"
                        placeholder='가장 기억에 남는 특징이나 경험'
                        value={reviewData.memorablePoints}
                        onChange={handleChange}
                        required
                    />
                </div>

                <div className={Styles.formGroup}>
                    <label htmlFor="recommendations">추천 대상</label>
                    <textarea
                        id="recommendations"
                        name="recommendations"
                        placeholder='사람들에게 추천'
                        value={reviewData.recommendations}
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
                        value={reviewData.revisit}
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
                        value={reviewData.additionalComments}
                        onChange={handleChange}
                    />
                </div>
            </form>
        </div>
    );
};

export default Review;
