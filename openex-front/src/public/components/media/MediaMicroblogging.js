import React, { useEffect } from 'react';
import { makeStyles, useTheme } from '@mui/styles';
import * as R from 'ramda';
import Typography from '@mui/material/Typography';
import Grid from '@mui/material/Grid';
import Card from '@mui/material/Card';
import CardHeader from '@mui/material/CardHeader';
import CardContent from '@mui/material/CardContent';
import Avatar from '@mui/material/Avatar';
import { useDispatch } from 'react-redux';
import CardMedia from '@mui/material/CardMedia';
import Button from '@mui/material/Button';
import {
  ChatBubbleOutlineOutlined,
  FavoriteBorderOutlined,
  ShareOutlined,
} from '@mui/icons-material';
import { useFormatter } from '../../../components/i18n';
import { fetchMediaDocuments } from '../../../actions/Document';
import { useHelper } from '../../../store';
import ExpandableMarkdown from '../../../components/ExpandableMarkdown';

const useStyles = makeStyles(() => ({
  container: {
    margin: '0 auto',
    width: 1200,
  },
  card: {
    position: 'relative',
  },
  logo: {
    maxHeight: 200,
    maxWidth: 300,
  },
  footer: {
    width: '100%',
    position: 'absolute',
    padding: '0 15px 0 15px',
    left: 0,
    bottom: 10,
  },
}));

const MediaMicroblogging = ({ mediaReader, preview }) => {
  const classes = useStyles();
  const theme = useTheme();
  const dispatch = useDispatch();
  const { t, fldt } = useFormatter();
  const isDark = theme.palette.mode === 'dark';
  const {
    media_exercise: exercise,
    media_articles: articles,
    media_information: media,
  } = mediaReader;
  const { documentsMap } = useHelper((helper) => ({
    documentsMap: helper.getDocumentsMap(),
  }));
  useEffect(() => {
    dispatch(fetchMediaDocuments(exercise.exercise_id));
  }, []);
  const logo = isDark ? media.media_logo_dark : media.media_logo_light;
  const filteredArticles = preview
    ? R.filter((n) => n.article_is_scheduled === true, articles)
    : R.filter((n) => n.article_is_scheduled !== true, articles);
  return (
    <div className={classes.container}>
      {logo && media.media_mode !== 'title' && (
        <div
          style={{ margin: '0 auto', textAlign: 'center', marginBottom: 15 }}
        >
          <img
            src={`/api/exercises/${exercise.exercise_id}/documents/${logo}/media_file`}
            className={classes.logo}
          />
        </div>
      )}
      {media.media_mode !== 'logo' && (
        <Typography
          variant="h1"
          style={{
            textAlign: 'center',
            color: isDark
              ? media.media_primary_color_dark
              : media.media_primary_color_light,
            fontSize: 40,
          }}
        >
          {media.media_name}
        </Typography>
      )}
      <Typography
        variant="h2"
        style={{
          textAlign: 'center',
        }}
      >
        {media.media_description}
      </Typography>
      <Grid container={true} spacing={3} style={{ marginTop: 0 }}>
        {filteredArticles.length > 0 && (
          <Grid item={true} xs={4}>
            {filteredArticles.map((article) => {
              const videos = article.article_documents
                .map((d) => (documentsMap[d.document_id]
                  ? documentsMap[d.document_id]
                  : undefined))
                .filter((d) => d !== undefined)
                .filter((d) => d.document_type.includes('video/'));
              let columns = 12;
              if (videos.length === 2) {
                columns = 6;
              } else if (videos.length === 3) {
                columns = 4;
              } else if (videos.length >= 4) {
                columns = 3;
              }
              return (
                <Card
                  key={article.article_id}
                  classes={{ root: classes.card }}
                  sx={{ width: '100%' }}
                >
                  <CardHeader
                    avatar={
                      <Avatar>
                        {(article.article_author || t('Unknown')).charAt(0)}
                      </Avatar>
                    }
                    title={article.article_author || t('Unknown')}
                    subheader={fldt(article.article_virtual_publication)}
                  />
                  <Grid container={true} spacing={3}>
                    {videos.map((doc) => (
                      <Grid item={true} xs={columns}>
                        <CardMedia
                          component="img"
                          height="150"
                          src={`/api/documents/${doc.document_id}/file`}
                        />
                      </Grid>
                    ))}
                  </Grid>
                  <CardContent style={{ marginBottom: 30 }}>
                    <Typography
                      gutterBottom
                      variant="h1"
                      component="div"
                      style={{ margin: '0 auto', textAlign: 'center' }}
                    >
                      {article.article_name}
                    </Typography>
                    <ExpandableMarkdown
                      source={article.article_content}
                      limit={200}
                      controlled={true}
                    />
                    <div className={classes.footer}>
                      <div style={{ float: 'right' }}>
                        <Button
                          size="small"
                          startIcon={<ChatBubbleOutlineOutlined />}
                        >
                          {article.article_comments || 0}
                        </Button>
                        <Button size="small" startIcon={<ShareOutlined />}>
                          {article.article_shares || 0}
                        </Button>
                        <Button
                          size="small"
                          startIcon={<FavoriteBorderOutlined />}
                        >
                          {article.article_likes || 0}
                        </Button>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              );
            })}
          </Grid>
        )}
      </Grid>
    </div>
  );
};

export default MediaMicroblogging;
