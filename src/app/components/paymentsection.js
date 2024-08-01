import React, { useState } from 'react';
import Styles from '@/app/styles/payment.module.css';

const PaymentSection = ({ setPayment, state, isPaymentRequired }) => {
  const [showForm, setShowForm] = useState(false);

  const handleShowForm = (confirm) => {
    if (confirm) {
      setShowForm(true);
    } else {
      setShowForm(false);
    }
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    const { amount, method, date, notes } = state.payment;
  
    if (!amount || !method || !date) {
      alert("모든 필수 필드를 입력해 주세요.");
      return;
    }
    alert('결제가 완료되었습니다.');
    setPayment({ amount: '', method: '', date: '', notes: '' });
    setShowForm(false);
  };

  if (!isPaymentRequired) return null; 

  return (
    <div className={Styles.paymentContainer}>
      {!showForm ? (
        <div className={Styles.paymentConfirmation}>
          <h3 className={Styles.PaymentTitle}>결제 정보</h3>
          <p className={Styles.paymentDescription}>포인트가 부족하여 결제가 필요합니다.</p>
          <p className={Styles.paymentQuestion}>결제하겠습니까?</p>
          <div className={Styles.paymentConfirmButtons}>
            <button className={Styles.paymentConfirmBtn} onClick={() => handleShowForm(true)}>예</button>
            <button className={Styles.paymentCancelBtn} onClick={() => handleShowForm(false)}>아니오</button>
          </div>
        </div>
      ) : (
        <div className={Styles.paymentFormContainer}>
          <form onSubmit={handleSubmit} className={Styles.paymentForm}>
            <input
              type="number"
              placeholder="결제 금액 입력"
              onChange={(e) => setPayment({ amount: e.target.value })}
              className={Styles.paymentInput}
            />
            <div className={Styles.paymentSelectGroup}>
              <label className={Styles.paymentLabel}>결제 방법 선택</label>
              <select
                onChange={(e) => setPayment({ method: e.target.value })}
                className={Styles.paymentSelect}
              >
                <option value="">선택</option>
                <option value="신용카드">신용카드</option>
                <option value="계좌이체">계좌이체</option>
                <option value="간편결제">간편결제</option>
                <option value="기타">기타</option>
              </select>
            </div>
            <div className={Styles.paymentDateGroup}>
              <label className={Styles.paymentLabel}>결제 날짜</label>
              <input
                type="date"
                onChange={(e) => setPayment({ date: e.target.value })}
                className={Styles.paymentDate}
              />
            </div>
            <textarea
              placeholder="결제 참고사항 입력"
              onChange={(e) => setPayment({ notes: e.target.value })}
              value={state.payment.notes || ''}
              className={Styles.paymentNotes}
            />
            <button
              type="submit"
              className={Styles.paymentSubmit}
            >
              결제하기
            </button>
          </form>
        </div>
      )}
    </div>
  );
};

export default PaymentSection;
