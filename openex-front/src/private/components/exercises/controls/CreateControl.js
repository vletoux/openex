/* eslint-disable no-template-curly-in-string */
import React, { Component } from 'react';
import * as PropTypes from 'prop-types';
import { connect } from 'react-redux';
import * as R from 'ramda';
import withStyles from '@mui/styles/withStyles';
import SpeedDial from '@mui/material/SpeedDial';
import SpeedDialIcon from '@mui/material/SpeedDialIcon';
import SpeedDialAction from '@mui/material/SpeedDialAction';
import Dialog from '@mui/material/Dialog';
import DialogTitle from '@mui/material/DialogTitle';
import DialogContent from '@mui/material/DialogContent';
import Slide from '@mui/material/Slide';
import {
  VideoSettingsOutlined,
  MarkEmailReadOutlined,
} from '@mui/icons-material';
import { withRouter } from 'react-router-dom';
import Grid from '@mui/material/Grid';
import Typography from '@mui/material/Typography';
import Button from '@mui/material/Button';
import DialogContentText from '@mui/material/DialogContentText';
import DialogActions from '@mui/material/DialogActions';
import { addComcheck } from '../../../../actions/Comcheck';
import { addDryrun } from '../../../../actions/Dryrun';
import inject18n from '../../../../components/i18n';
import ComcheckForm from './ComcheckForm';
import { storeBrowser } from '../../../../actions/Schema';

const Transition = React.forwardRef((props, ref) => (
  <Slide direction="up" ref={ref} {...props} />
));
Transition.displayName = 'TransitionSlide';

const styles = (theme) => ({
  createButton: {
    position: 'fixed',
    bottom: 30,
    right: 30,
  },
  text: {
    fontSize: 15,
    color: theme.palette.primary.main,
    fontWeight: 500,
  },
});

class CreateControl extends Component {
  constructor(props) {
    super(props);
    this.state = { openComcheck: false, openDryrun: false };
  }

  handleOpenComcheck() {
    this.setState({ openComcheck: true });
  }

  handleCloseComcheck() {
    this.setState({ openComcheck: false });
  }

  onSubmitComcheck(data) {
    return this.props
      .addComcheck(this.props.exerciseId, data)
      .then(() => this.props.history.push('/exercises'));
  }

  handleOpenDryrun() {
    this.setState({ openDryrun: true });
  }

  handleCloseDryrun() {
    this.setState({ openDryrun: false });
  }

  submitDryrun() {
    return this.props
      .addDryrun(this.props.exerciseId)
      .then(() => this.props.history.push('/exercises'));
  }

  render() {
    const {
      classes, t, audiences, variant,
    } = this.props;
    return (
      <div>
        {variant === 'buttons' ? (
          <Grid container={true} spacing={3} style={{ marginTop: 0 }}>
            <Grid item={true} xs={6}>
              <Typography variant="h1">{t('Dryrun')}</Typography>
              <Button
                variant="contained"
                startIcon={<VideoSettingsOutlined />}
                color="info"
                onClick={this.handleOpenDryrun.bind(this)}
              >
                {t('Launch')}
              </Button>
            </Grid>
            <Grid item={true} xs={4}>
              <Typography variant="h1">{t('Comcheck')}</Typography>
              <Button
                variant="contained"
                startIcon={<MarkEmailReadOutlined />}
                color="secondary"
                onClick={this.handleOpenComcheck.bind(this)}
              >
                {t('Send')}
              </Button>
            </Grid>
          </Grid>
        ) : (
          <SpeedDial
            classes={{ root: classes.createButton }}
            icon={<SpeedDialIcon />}
            ariaLabel={t('New control')}
          >
            <SpeedDialAction
              icon={<VideoSettingsOutlined />}
              tooltipTitle={t('Launch a new dryrun')}
              onClick={this.handleOpenDryrun.bind(this)}
            />
            <SpeedDialAction
              icon={<MarkEmailReadOutlined />}
              tooltipTitle={t('Send a new comcheck')}
              onClick={this.handleOpenComcheck.bind(this)}
            />
          </SpeedDial>
        )}
        <Dialog
          open={this.state.openComcheck}
          TransitionComponent={Transition}
          onClose={this.handleCloseComcheck.bind(this)}
          fullWidth={true}
          maxWidth="md"
        >
          <DialogTitle>{t('Send a new comcheck')}</DialogTitle>
          <DialogContent>
            <ComcheckForm
              onSubmit={this.onSubmitComcheck.bind(this)}
              initialValues={{
                comcheck_audiences: [],
                comcheck_subject: t('[${exercise.name}] Communication check'),
                comcheck_message: `${t('Hello')},<br /><br />${t(
                  'This is a communication check before the beginning of the exercise. Please click on the following link'
                    + ' in order to confirm you successfully received this message: ${comcheck.url}',
                )}<br /><br />${t('Best regards')},<br />${t(
                  'The exercise control team',
                )}`,
              }}
              audiences={audiences}
              handleClose={this.handleCloseComcheck.bind(this)}
            />
          </DialogContent>
        </Dialog>
        <Dialog
          open={this.state.openDryrun}
          TransitionComponent={Transition}
          onClose={this.handleCloseDryrun.bind(this)}
        >
          <DialogContent>
            <DialogContentText>
              {t('Do you want to launch a new dryrun?')}
            </DialogContentText>
          </DialogContent>
          <DialogActions>
            <Button
              variant="contained"
              color="secondary"
              onClick={this.handleCloseDryrun.bind(this)}
            >
              {t('Cancel')}
            </Button>
            <Button
              variant="contained"
              color="primary"
              onClick={this.submitDryrun.bind(this)}
            >
              {t('Launch')}
            </Button>
          </DialogActions>
        </Dialog>
      </div>
    );
  }
}

CreateControl.propTypes = {
  exerciseId: PropTypes.string,
  exercise: PropTypes.object,
  classes: PropTypes.object,
  t: PropTypes.func,
  addComcheck: PropTypes.func,
  addDryrun: PropTypes.func,
  audiences: PropTypes.array,
  history: PropTypes.object,
  variant: PropTypes.string,
};

const select = (state, ownProps) => {
  const browser = storeBrowser(state);
  const { exerciseId } = ownProps;
  const exercise = browser.getExercise(exerciseId);
  return {
    exercise,
    audiences: exercise?.audiences,
  };
};

export default R.compose(
  connect(select, { addComcheck, addDryrun }),
  inject18n,
  withRouter,
  withStyles(styles),
)(CreateControl);